package nl.flits.nieuws.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
fun SearchScreen(vm: NewsViewModel, onOpen: (Article) -> Unit) {
    val query by vm.query.collectAsStateWithLifecycle()
    val results by vm.searchResults.collectAsStateWithLifecycle()
    val searching by vm.searching.collectAsStateWithLifecycle()
    val bookmarkIds by vm.bookmarkIds.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(top = 16.dp)) {
        Text("Zoeken", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 20.dp))
        OutlinedTextField(
            value = query,
            onValueChange = vm::onQueryChange,
            placeholder = { Text("Zoek in al het nieuws…") },
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) IconButton(onClick = { vm.onQueryChange("") }) {
                    Icon(Icons.Outlined.Close, "Wissen")
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
        if (searching) LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 16.dp))
        when {
            query.trim().length < 2 -> MessageState(
                icon = Icons.Outlined.Search,
                title = "Waar ben je naar op zoek?",
                message = "Doorzoek de koppen van NOS, NU.nl, AD, Telegraaf, Volkskrant en vele andere bronnen.",
            )
            results.isEmpty() && !searching -> MessageState(
                icon = Icons.Outlined.SearchOff,
                title = "Niets gevonden",
                message = "Geen artikelen gevonden voor \"$query\".",
            )
            else -> LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                items(results, key = { it.id }) { a ->
                    ArticleCard(a, a.id in bookmarkIds, onClick = { onOpen(a) }, onBookmark = { vm.toggleBookmark(a) })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                }
            }
        }
    }
}
