package nl.flits.nieuws.data

import org.jsoup.Jsoup
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Ruwe feed-item, onafhankelijk van bron/categorie. */
data class RawItem(
    val title: String,
    val link: String,
    val summary: String,
    val imageUrl: String?,
    val publishedAt: Long?,
)

/**
 * Lichtgewicht parser voor RSS 2.0, RSS 1.0 (RDF) en Atom.
 * Haalt afbeeldingen uit enclosure, media:content, media:thumbnail of de eerste <img> in de HTML.
 */
object RssParser {

    fun parse(input: InputStream, charset: String? = null): List<RawItem> {
        val parser = XmlPullParserFactory.newInstance().apply { isNamespaceAware = true }.newPullParser()
        parser.setInput(input, charset)
        val items = mutableListOf<RawItem>()

        var inItem = false
        var title = ""
        var link = ""
        var description = ""
        var content = ""
        var image: String? = null
        var date: String? = null

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            val name = parser.name?.lowercase(Locale.ROOT)
            when (event) {
                XmlPullParser.START_TAG -> {
                    if (name == "item" || name == "entry") {
                        inItem = true
                        title = ""; link = ""; description = ""; content = ""; image = null; date = null
                    } else if (inItem) {
                        val prefix = parser.prefix
                        when {
                            name == "title" -> title = readText(parser)
                            name == "link" -> {
                                val href = parser.getAttributeValue(null, "href")
                                val rel = parser.getAttributeValue(null, "rel")
                                if (href != null) {
                                    if (rel == null || rel == "alternate") link = href
                                    else if (rel == "enclosure" && image == null &&
                                        parser.getAttributeValue(null, "type")?.startsWith("image") == true
                                    ) image = href
                                } else {
                                    val text = readText(parser)
                                    if (text.isNotBlank()) link = text
                                }
                            }
                            name == "guid" && link.isBlank() -> {
                                val perma = parser.getAttributeValue(null, "isPermaLink")
                                val text = readText(parser)
                                if (perma != "false" && text.startsWith("http")) link = text
                            }
                            name == "description" || name == "summary" -> description = readText(parser)
                            (name == "encoded" && prefix == "content") || name == "content" && prefix != "media" -> {
                                content = readText(parser)
                            }
                            name == "pubdate" || name == "published" || name == "updated" ||
                                (name == "date" && prefix == "dc") -> {
                                val text = readText(parser)
                                if (date == null || name != "updated") date = text
                            }
                            name == "enclosure" -> {
                                val type = parser.getAttributeValue(null, "type") ?: ""
                                val url = parser.getAttributeValue(null, "url")
                                if (url != null && (type.startsWith("image") || type.isEmpty() && looksLikeImage(url))) {
                                    image = image ?: url
                                }
                            }
                            prefix == "media" && (name == "content" || name == "thumbnail") -> {
                                val url = parser.getAttributeValue(null, "url")
                                val medium = parser.getAttributeValue(null, "medium")
                                val type = parser.getAttributeValue(null, "type")
                                val isImage = medium == "image" || type?.startsWith("image") == true ||
                                    name == "thumbnail" || (medium == null && type == null && url != null && looksLikeImage(url))
                                if (url != null && isImage && image == null) image = url
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if ((name == "item" || name == "entry") && inItem) {
                        inItem = false
                        val html = content.ifBlank { description }
                        val img = image ?: firstImage(html) ?: firstImage(description)
                        val cleanTitle = Jsoup.parse(title).text().trim()
                        if (cleanTitle.isNotBlank() && link.isNotBlank()) {
                            items += RawItem(
                                title = cleanTitle,
                                link = link.trim(),
                                summary = toPlainText(description.ifBlank { content }),
                                imageUrl = img?.let(::normalizeUrl),
                                publishedAt = date?.let(::parseDate),
                            )
                        }
                    }
                }
            }
            event = parser.next()
        }
        return items
    }

    private fun readText(parser: XmlPullParser): String {
        val sb = StringBuilder()
        var depth = 1
        while (depth > 0) {
            when (parser.next()) {
                XmlPullParser.TEXT, XmlPullParser.CDSECT -> sb.append(parser.text)
                XmlPullParser.ENTITY_REF -> sb.append(parser.text ?: "")
                XmlPullParser.START_TAG -> {
                    // XHTML-inhoud in Atom: bewaar als tekst.
                    depth++
                }
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.END_DOCUMENT -> return sb.toString()
            }
        }
        return sb.toString().trim()
    }

    private fun looksLikeImage(url: String) =
        Regex("""\.(jpe?g|png|webp|gif)(\?|$)""", RegexOption.IGNORE_CASE).containsMatchIn(url)

    private fun firstImage(html: String): String? {
        if (!html.contains("<img", ignoreCase = true)) return null
        return Jsoup.parse(html).selectFirst("img[src]")?.attr("src")?.takeIf { it.startsWith("http") || it.startsWith("//") }
    }

    private fun normalizeUrl(url: String): String = when {
        url.startsWith("//") -> "https:$url"
        url.startsWith("http://") -> "https://" + url.removePrefix("http://")
        else -> url
    }.replace("&amp;", "&")

    fun toPlainText(html: String): String {
        if (html.isBlank()) return ""
        val text = Jsoup.parse(html).text()
        return text.replace(Regex("\\s+"), " ").trim()
    }

    private val rfc822Formats = listOf(
        "EEE, d MMM yyyy HH:mm:ss Z",
        "EEE, d MMM yyyy HH:mm:ss zzz",
        "EEE, d MMM yyyy HH:mm Z",
        "d MMM yyyy HH:mm:ss Z",
        "EEE, d MMM yyyy HH:mm:ss",
    ).map { DateTimeFormatter.ofPattern(it, Locale.ENGLISH) }

    fun parseDate(raw: String): Long? {
        val s = raw.trim().replace(Regex("\\s+"), " ")
        runCatching { return OffsetDateTime.parse(s).toInstant().toEpochMilli() }
        runCatching { return ZonedDateTime.parse(s, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli() }
        for (f in rfc822Formats) {
            runCatching { return ZonedDateTime.parse(s, f).toInstant().toEpochMilli() }
            runCatching {
                return java.time.LocalDateTime.parse(s, f).atZone(java.time.ZoneId.of("Europe/Amsterdam")).toInstant().toEpochMilli()
            }
        }
        return null
    }
}
