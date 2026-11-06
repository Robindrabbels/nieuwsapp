package nl.flits.nieuws.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val disabledSources: Set<String> = emptySet(),
    val hiddenCategories: Set<String> = emptySet(),
    val openInApp: Boolean = true,
)

private val Context.dataStore by preferencesDataStore(name = "flits")

class SettingsRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val DISABLED = stringSetPreferencesKey("disabled_sources")
        val HIDDEN = stringSetPreferencesKey("hidden_categories")
        val OPEN_IN_APP = stringPreferencesKey("open_in_app")
        val BOOKMARKS = stringPreferencesKey("bookmarks")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { p ->
        UserSettings(
            themeMode = p[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            disabledSources = p[Keys.DISABLED] ?: emptySet(),
            hiddenCategories = p[Keys.HIDDEN] ?: emptySet(),
            openInApp = p[Keys.OPEN_IN_APP] != "false",
        )
    }

    val bookmarks: Flow<List<Article>> = context.dataStore.data.map { p ->
        p[Keys.BOOKMARKS]?.let { runCatching { json.decodeFromString<List<Article>>(it) }.getOrNull() } ?: emptyList()
    }

    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[Keys.THEME] = mode.name }

    suspend fun setOpenInApp(value: Boolean) = context.dataStore.edit { it[Keys.OPEN_IN_APP] = value.toString() }

    suspend fun toggleSource(sourceId: String) = context.dataStore.edit {
        val current = it[Keys.DISABLED] ?: emptySet()
        it[Keys.DISABLED] = if (sourceId in current) current - sourceId else current + sourceId
    }

    suspend fun toggleCategory(categoryId: String) = context.dataStore.edit {
        val current = it[Keys.HIDDEN] ?: emptySet()
        it[Keys.HIDDEN] = if (categoryId in current) current - categoryId else current + categoryId
    }

    suspend fun toggleBookmark(article: Article) = context.dataStore.edit { p ->
        val list = p[Keys.BOOKMARKS]?.let { runCatching { json.decodeFromString<List<Article>>(it) }.getOrNull() } ?: emptyList()
        val updated = if (list.any { it.id == article.id }) list.filterNot { it.id == article.id } else listOf(article) + list
        p[Keys.BOOKMARKS] = json.encodeToString(updated)
    }
}
