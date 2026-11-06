package nl.flits.nieuws.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/** Resultaat van het laden van een categorie. */
data class CategoryResult(
    val articles: List<Article>,
    val failedSources: List<String>,
    val fetchedAt: Long,
)

class NewsRepository(context: Context) {

    private val client = OkHttpClient.Builder()
        .cache(Cache(File(context.cacheDir, "http"), 20L * 1024 * 1024))
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val memory = mutableMapOf<Category, CategoryResult>()
    private val mutex = Mutex()

    /** Gecachte artikelen van alle reeds geladen categorieën (voor zoeken). */
    suspend fun allCached(): List<Article> = mutex.withLock {
        memory.values.flatMap { it.articles }.distinctBy { it.link }.sortedByDescending { it.publishedAt ?: 0 }
    }

    suspend fun load(
        category: Category,
        disabledSources: Set<String>,
        force: Boolean = false,
    ): CategoryResult {
        if (!force) {
            mutex.withLock { memory[category] }?.let { cached ->
                if (System.currentTimeMillis() - cached.fetchedAt < CACHE_MS) {
                    return cached.copy(articles = cached.articles.filter { it.sourceId !in disabledSources })
                }
            }
        }
        val feeds = FeedCatalog.forCategory(category).filter { it.source.id !in disabledSources }
        val results = withContext(Dispatchers.IO) {
            coroutineScope {
                feeds.map { feed -> async { feed to runCatching { fetch(feed, force) } } }.awaitAll()
            }
        }
        val failed = results.filter { it.second.isFailure }.map { it.first.source.name }.distinct()
        val articles = results.flatMap { it.second.getOrDefault(emptyList()) }
            .distinctBy { it.link }
            .let(::dedupeTitles)
            .sortedByDescending { it.publishedAt ?: 0L }
            .take(MAX_PER_CATEGORY)
        val result = CategoryResult(articles, failed, System.currentTimeMillis())
        mutex.withLock { memory[category] = result }
        return result
    }

    private fun fetch(feed: Feed, force: Boolean): List<Article> {
        val request = Request.Builder()
            .url(feed.url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/rss+xml, application/atom+xml, application/xml;q=0.9, text/xml;q=0.8, */*;q=0.5")
            .apply { if (force) header("Cache-Control", "no-cache") }
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code} voor ${feed.url}")
            val body = response.body ?: error("Lege response")
            val charset = body.contentType()?.charset()?.name()
            return RssParser.parse(body.byteStream(), charset).take(MAX_PER_FEED).map { raw ->
                Article(
                    id = sha1(raw.link),
                    title = raw.title,
                    link = raw.link,
                    summary = raw.summary.take(600),
                    imageUrl = raw.imageUrl,
                    publishedAt = raw.publishedAt,
                    sourceId = feed.source.id,
                    sourceName = feed.source.name,
                    categoryId = feed.category.id,
                )
            }
        }
    }

    /** Verwijdert (vrijwel) identieke koppen van verschillende bronnen niet: alleen exacte duplicaten. */
    private fun dedupeTitles(list: List<Article>): List<Article> =
        list.distinctBy { it.sourceId + "|" + it.title.lowercase() }

    companion object {
        private const val CACHE_MS = 5 * 60 * 1000L
        private const val MAX_PER_FEED = 40
        private const val MAX_PER_CATEGORY = 200
        private const val USER_AGENT = "Flits/1.0 (Android; RSS-lezer)"

        fun sha1(s: String): String =
            MessageDigest.getInstance("SHA-1").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
