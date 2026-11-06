package nl.flits.nieuws.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import nl.flits.nieuws.R
import nl.flits.nieuws.data.Article
import nl.flits.nieuws.data.Sources
import nl.flits.nieuws.ui.theme.FlitsRed
import nl.flits.nieuws.ui.theme.FlitsRedDark
import nl.flits.nieuws.ui.theme.FlitsRedLight
import nl.flits.nieuws.util.relativeTime

/** Het Flits-logo: rode tegel met bliksem + woordmerk. */
@Composable
fun FlitsLogo(modifier: Modifier = Modifier, size: Dp = 30.dp, showWordmark: Boolean = true) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.3f))
                .background(Brush.linearGradient(listOf(FlitsRedLight, FlitsRed, FlitsRedDark))),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_bolt),
                contentDescription = null,
                modifier = Modifier.size(size * 0.62f),
            )
        }
        if (showWordmark) {
            Spacer(Modifier.width(10.dp))
            Text(
                "flits",
                fontSize = (size.value * 0.95f).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(".", fontSize = (size.value * 0.95f).sp, fontWeight = FontWeight.Black, color = FlitsRed)
        }
    }
}

@Composable
fun SourceTag(article: Article, onDark: Boolean = false) {
    val color = Sources.byId(article.sourceId)?.color?.let { Color(it) } ?: FlitsRed
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(if (onDark) Color.White else color))
        Spacer(Modifier.width(6.dp))
        Text(
            article.sourceName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (onDark) Color.White else MaterialTheme.colorScheme.onSurface,
        )
        val time = relativeTime(article.publishedAt)
        if (time.isNotEmpty()) {
            Text(
                "  ·  $time",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                color = if (onDark) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ArticleImage(url: String?, modifier: Modifier) {
    val placeholder = @Composable {
        Box(
            Modifier.fillMaxSize().background(
                Brush.linearGradient(listOf(FlitsRedLight.copy(alpha = 0.85f), FlitsRedDark))
            ),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(R.drawable.ic_bolt), null, Modifier.size(36.dp).alpha(0.5f))
        }
    }
    if (url == null) {
        Box(modifier) { placeholder() }
    } else {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            error = { placeholder() },
        )
    }
}

/** Grote uitgelichte kaart met afbeelding en titel over een verloop. */
@Composable
fun HeroArticleCard(
    article: Article,
    bookmarked: Boolean,
    onClick: () -> Unit,
    onBookmark: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(4f / 3.6f)) {
            ArticleImage(article.imageUrl, Modifier.fillMaxSize())
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color.Transparent, 0.35f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.88f)
                    )
                )
            )
            Surface(
                color = FlitsRed,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart),
            ) {
                Text(
                    "UITGELICHT",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
            IconButton(
                onClick = onBookmark,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            ) {
                Icon(
                    if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (bookmarked) "Verwijder uit bewaard" else "Bewaar artikel",
                    tint = Color.White,
                )
            }
            Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                SourceTag(article, onDark = true)
                Spacer(Modifier.height(8.dp))
                Text(
                    article.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Compacte nieuwsregel met thumbnail rechts. */
@Composable
fun ArticleCard(
    article: Article,
    bookmarked: Boolean,
    onClick: () -> Unit,
    onBookmark: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            SourceTag(article)
            Spacer(Modifier.height(6.dp))
            Text(
                article.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (article.summary.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    article.summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(horizontalAlignment = Alignment.End) {
            ArticleImage(article.imageUrl, Modifier.size(width = 104.dp, height = 88.dp).clip(RoundedCornerShape(14.dp)))
            IconButton(onClick = onBookmark, modifier = Modifier.size(36.dp)) {
                Icon(
                    if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (bookmarked) "Verwijder uit bewaard" else "Bewaar artikel",
                    tint = if (bookmarked) FlitsRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
fun SkeletonList(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val a by transition.animateFloat(0.35f, 0.8f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "a")
    val c = MaterialTheme.colorScheme.surfaceVariant
    Column(modifier.padding(16.dp).alpha(a), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Box(Modifier.fillMaxWidth().aspectRatio(4f / 3.6f).clip(RoundedCornerShape(24.dp)).background(c))
        repeat(4) {
            Row {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.width(90.dp).height(10.dp).clip(CircleShape).background(c))
                    Box(Modifier.fillMaxWidth().height(16.dp).clip(CircleShape).background(c))
                    Box(Modifier.fillMaxWidth(0.7f).height(16.dp).clip(CircleShape).background(c))
                }
                Spacer(Modifier.width(14.dp))
                Box(Modifier.size(104.dp, 88.dp).clip(RoundedCornerShape(14.dp)).background(c))
            }
        }
    }
}

@Composable
fun MessageState(
    icon: ImageVector = Icons.Outlined.CloudOff,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}
