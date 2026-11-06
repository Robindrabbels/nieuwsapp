package nl.flits.nieuws.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import nl.flits.nieuws.data.Article
import nl.flits.nieuws.data.Category
import nl.flits.nieuws.ui.CategoryState
import nl.flits.nieuws.ui.NewsViewModel
import nl.flits.nieuws.ui.components.ArticleCard
import nl.flits.nieuws.ui.components.FlitsLogo
import nl.flits.nieuws.ui.components.HeroArticleCard
import nl.flits.nieuws.ui.components.MessageState
import nl.flits.nieuws.ui.components.SkeletonList
import nl.flits.nieuws.util.todayLabel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(vm: NewsViewModel, onOpen: (Article) -> Unit) {
    val categories by vm.visibleCategories.collectAsStateWithLifecycle()
    val states by vm.categories.collectAsStateWithLifecycle()
    val bookmarkIds by vm.bookmarkIds.collectAsStateWithLifecycle()
    val pager = rememberPagerState { categories.size }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pager.currentPage, categories) {
        categories.getOrNull(pager.currentPage)?.let(vm::ensureLoaded)
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    FlitsLogo(size = 28.dp)
                    Text(
                        todayLabel(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            windowInsets = WindowInsets(0),
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        )
        PrimaryScrollableTabRow(
            selectedTabIndex = pager.currentPage.coerceAtMost(categories.lastIndex.coerceAtLeast(0)),
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) },
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(pager.currentPage, matchContentSize = true),
                    width = 28.dp,
                    height = 4.dp,
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                )
            },
        ) {
            categories.forEachIndexed { index, cat ->
                val selected = pager.currentPage == index
                Tab(
                    selected = selected,
                    onClick = { scope.launch { pager.animateScrollToPage(index) } },
                    text = {
                        Text(
                            cat.label,
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize(), beyondViewportPageCount = 0) { page ->
            val cat = categories.getOrNull(page) ?: return@HorizontalPager
            CategoryFeed(
                category = cat,
                state = states[cat] ?: CategoryState(loading = true),
                bookmarkIds = bookmarkIds,
                onRefresh = { vm.refresh(cat) },
                onOpen = onOpen,
                onBookmark = { vm.toggleBookmark(it) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFeed(
    category: Category,
    state: CategoryState,
    bookmarkIds: Set<String>,
    onRefresh: () -> Unit,
    onOpen: (Article) -> Unit,
    onBookmark: (Article) -> Unit,
) {
    val listState = rememberLazyListState()
    PullToRefreshBox(
        isRefreshing = state.refreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            state.loading && state.articles.isEmpty() -> SkeletonList()
            state.error != null && state.articles.isEmpty() -> LazyColumn(Modifier.fillMaxSize()) {
                item {
                    MessageState(
                        title = "Geen verbinding",
                        message = state.error,
                        actionLabel = "Opnieuw proberen",
                        onAction = onRefresh,
                    )
                }
            }
            state.articles.isEmpty() -> LazyColumn(Modifier.fillMaxSize()) {
                item {
                    MessageState(
                        icon = Icons.Outlined.Newspaper,
                        title = "Nog geen nieuws",
                        message = "Er is nog geen nieuws in ${category.label}. Trek omlaag om te verversen.",
                    )
                }
            }
            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (state.failedSources.isNotEmpty()) {
                    item(key = "failed") {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        ) {
                            androidx.compose.foundation.layout.Row(
                                Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                Spacer(Modifier.padding(4.dp))
                                Text(
                                    "Niet bereikbaar: " + state.failedSources.joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            }
                        }
                    }
                }
                itemsIndexed(state.articles, key = { _, a -> a.id }) { index, article ->
                    if (index == 0) {
                        HeroArticleCard(
                            article = article,
                            bookmarked = article.id in bookmarkIds,
                            onClick = { onOpen(article) },
                            onBookmark = { onBookmark(article) },
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                    } else {
                        ArticleCard(
                            article = article,
                            bookmarked = article.id in bookmarkIds,
                            onClick = { onOpen(article) },
                            onBookmark = { onBookmark(article) },
                        )
                        if (index < state.articles.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}
