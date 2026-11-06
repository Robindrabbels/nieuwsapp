package nl.flits.nieuws.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.flits.nieuws.data.Article
import nl.flits.nieuws.ui.NewsViewModel
import nl.flits.nieuws.ui.components.ArticleCard
import nl.flits.nieuws.ui.components.MessageState

@Composable
fun SavedScreen(vm: NewsViewModel, onOpen: (Article) -> Unit) {
    val saved by vm.bookmarks.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(top = 16.dp)) {
        Text("Bewaard", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 20.dp))
        Text(
            if (saved.isEmpty()) "Je leeslijst" else "${saved.size} artikel${if (saved.size == 1) "" else "en"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        if (saved.isEmpty()) {
            MessageState(
                icon = Icons.Outlined.BookmarkBorder,
                title = "Nog niets bewaard",
                message = "Tik op het bladwijzer-icoon bij een artikel om het later te lezen.",
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(saved, key = { it.id }) { a ->
                    ArticleCard(a, bookmarked = true, onClick = { onOpen(a) }, onBookmark = { vm.toggleBookmark(a) })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                }
            }
        }
    }
}
