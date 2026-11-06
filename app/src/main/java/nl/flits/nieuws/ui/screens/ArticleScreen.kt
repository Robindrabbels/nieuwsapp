package nl.flits.nieuws.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import nl.flits.nieuws.data.Category
import nl.flits.nieuws.ui.NewsViewModel
import nl.flits.nieuws.ui.components.MessageState
import nl.flits.nieuws.ui.components.SourceTag
import nl.flits.nieuws.ui.theme.FlitsRed
import nl.flits.nieuws.ui.theme.FlitsRedDark
import nl.flits.nieuws.util.openArticle
import nl.flits.nieuws.util.shareArticle

@Composable
fun ArticleScreen(vm: NewsViewModel, articleId: String, onBack: () -> Unit) {
    val article = vm.article(articleId)
    val bookmarkIds by vm.bookmarkIds.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toolbarColor = MaterialTheme.colorScheme.surface.toArgb()

    if (article == null) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            FilledTonalIconButton(onClick = onBack, modifier = Modifier.padding(12.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Terug")
            }
            MessageState(title = "Artikel niet gevonden", message = "Dit artikel is niet meer beschikbaar.")
        }
        return
    }
    val bookmarked = article.id in bookmarkIds
    val roundButton = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = Color.Black.copy(alpha = 0.45f), contentColor = Color.White,
    )

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().aspectRatio(1.25f)) {
                if (article.imageUrl != null) {
                    AsyncImage(
                        model = article.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(FlitsRed, FlitsRedDark))))
                }
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.35f),
                            0.3f to Color.Transparent,
                            0.75f to Color.Transparent,
                            1f to MaterialTheme.colorScheme.surface,
                        )
                    )
                )
            }
            Column(Modifier.padding(horizontal = 22.dp)) {
                val category = Category.fromId(article.categoryId)
                if (category != null) {
                    Text(
                        category.label.uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Text(article.title, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(14.dp))
                SourceTag(article)
                Spacer(Modifier.height(20.dp))
                Box(Modifier.width(48.dp).height(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                Spacer(Modifier.height(20.dp))
                Text(
                    article.summary.ifBlank { "Lees het volledige artikel op de website van ${article.sourceName}." },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(28.dp))
                Button(
                    onClick = { openArticle(context, article.link, settings.openInApp, toolbarColor) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Text("Lees volledig artikel", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null, Modifier.size(18.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Bron: ${article.sourceName}. Inhoud © de uitgever.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(32.dp))
                Spacer(Modifier.navigationBarsPadding())
            }
        }
        // Zwevende knoppen bovenaan
        Box(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp)) {
            FilledTonalIconButton(onClick = onBack, colors = roundButton, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Terug")
            }
            androidx.compose.foundation.layout.Row(Modifier.align(Alignment.CenterEnd)) {
                FilledTonalIconButton(onClick = { shareArticle(context, article) }, colors = roundButton) {
                    Icon(Icons.Outlined.Share, "Delen")
                }
                FilledTonalIconButton(onClick = { vm.toggleBookmark(article) }, colors = roundButton) {
                    Icon(
                        if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        if (bookmarked) "Verwijder uit bewaard" else "Bewaar artikel",
                    )
                }
            }
        }
    }
}
