package nl.flits.nieuws.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.flits.nieuws.FlitsApplication
import nl.flits.nieuws.data.Article
import nl.flits.nieuws.data.Category
import nl.flits.nieuws.data.ThemeMode
import nl.flits.nieuws.data.UserSettings

data class CategoryState(
    val articles: List<Article> = emptyList(),
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val failedSources: List<String> = emptyList(),
    val error: String? = null,
    val loaded: Boolean = false,
)

@OptIn(FlowPreview::class)
class NewsViewModel(app: Application) : AndroidViewModel(app) {
    private val news = (app as FlitsApplication).news
    private val prefs = (app as FlitsApplication).settings

    val settings: StateFlow<UserSettings> =
        prefs.settings.stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    val bookmarks: StateFlow<List<Article>> =
        prefs.bookmarks.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val bookmarkIds: StateFlow<Set<String>> =
        bookmarks.map { list -> list.map { it.id }.toSet() }.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val visibleCategories: StateFlow<List<Category>> = settings
        .map { s -> Category.entries.filter { it == Category.TOP || it.id !in s.hiddenCategories } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Category.entries)

    private val _categories = MutableStateFlow<Map<Category, CategoryState>>(emptyMap())
    val categories: StateFlow<Map<Category, CategoryState>> = _categories.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Article>>(emptyList())
    val searchResults: StateFlow<List<Article>> = _searchResults.asStateFlow()

    private val _searching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = _searching.asStateFlow()

    /** Alle artikelen die de app heeft gezien, zodat het detailscherm ze op id kan vinden. */
    private val known = mutableMapOf<String, Article>()

    init {
        viewModelScope.launch {
            _query.debounce(250).distinctUntilChanged().collect { runSearch(it) }
        }
        // Bronnen aan/uit gezet → geladen categorieën opnieuw ophalen.
        viewModelScope.launch {
            settings.map { it.disabledSources }.distinctUntilChanged().drop(1).collect {
                _categories.value.keys.forEach { cat -> load(cat, force = false, reset = true) }
            }
        }
    }

    fun ensureLoaded(category: Category) {
        val state = _categories.value[category]
        if (state == null || (!state.loaded && !state.loading)) load(category)
    }

    fun refresh(category: Category) = load(category, force = true)

    private fun load(category: Category, force: Boolean = false, reset: Boolean = false) {
        _categories.update { map ->
            val cur = map[category] ?: CategoryState()
            map + (category to cur.copy(
                loading = !cur.loaded || reset,
                refreshing = force && cur.loaded,
                error = null,
            ))
        }
        viewModelScope.launch {
            val result = runCatching { news.load(category, settings.value.disabledSources, force) }
            _categories.update { map ->
                val cur = map[category] ?: CategoryState()
                val new = result.fold(
                    onSuccess = { r ->
                        r.articles.forEach { known[it.id] = it }
                        cur.copy(
                            articles = r.articles,
                            failedSources = r.failedSources,
                            error = if (r.articles.isEmpty() && r.failedSources.isNotEmpty())
                                "Kon geen nieuws laden. Controleer je internetverbinding." else null,
                        )
                    },
                    onFailure = { cur.copy(error = "Er ging iets mis: ${it.message}") },
                )
                map + (category to new.copy(loading = false, refreshing = false, loaded = true))
            }
            if (_query.value.isNotBlank()) runSearch(_query.value)
        }
    }

    fun onQueryChange(q: String) { _query.value = q }

    private suspend fun runSearch(q: String) {
        val term = q.trim()
        if (term.length < 2) { _searchResults.value = emptyList(); return }
        _searching.value = true
        // Zorg dat alle categorieën geladen zijn zodat er over al het nieuws gezocht wordt.
        Category.entries.forEach { ensureLoaded(it) }
        val words = term.lowercase().split(" ").filter { it.isNotBlank() }
        _searchResults.value = news.allCached().filter { a ->
            val hay = (a.title + " " + a.summary + " " + a.sourceName).lowercase()
            words.all { it in hay }
        }.take(150)
        _searching.value = false
    }

    fun article(id: String): Article? = known[id] ?: bookmarks.value.firstOrNull { it.id == id }

    fun toggleBookmark(article: Article) = viewModelScope.launch { prefs.toggleBookmark(article) }
    fun setTheme(mode: ThemeMode) = viewModelScope.launch { prefs.setTheme(mode) }
    fun setOpenInApp(value: Boolean) = viewModelScope.launch { prefs.setOpenInApp(value) }
    fun toggleSource(id: String) = viewModelScope.launch { prefs.toggleSource(id) }
    fun toggleCategory(id: String) = viewModelScope.launch { prefs.toggleCategory(id) }
}
