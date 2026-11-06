package nl.flits.nieuws.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import nl.flits.nieuws.data.Article
import nl.flits.nieuws.data.ThemeMode
import nl.flits.nieuws.ui.components.ArticleCard
import nl.flits.nieuws.ui.components.FlitsLogo
import nl.flits.nieuws.ui.components.HeroArticleCard
import nl.flits.nieuws.ui.theme.FlitsTheme
import org.junit.Rule
import org.junit.Test

/** Rendert de belangrijkste UI-componenten met voorbeelddata (./gradlew recordPaparazziDebug). */
class ScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6, showSystemUi = false)

    private val now = System.currentTimeMillis()
    private fun a(i: Int, title: String, src: String, name: String, mins: Int, summary: String = "") = Article(
        id = "$i", title = title, link = "https://example.org/$i", summary = summary, imageUrl = null,
        publishedAt = now - mins * 60_000L, sourceId = src, sourceName = name, categoryId = "top",
    )

    private val articles = listOf(
        a(1, "Kabinet presenteert miljardenplan voor betaalbare woningen", "nos", "NOS", 4),
        a(2, "Storm legt treinverkeer rond Utrecht urenlang plat", "nu", "NU.nl", 18,
            "Reizigers moeten rekening houden met flinke vertragingen tot in de avond."),
        a(3, "Oranje wint overtuigend en plaatst zich voor eindronde", "ad", "AD", 42,
            "Een sterke tweede helft bezorgde het Nederlands elftal de zege."),
        a(4, "Nieuwe generatie smartphones krijgt AI-chip aan boord", "tweakers", "Tweakers", 75,
            "Fabrikanten zetten vol in op lokale AI-functies."),
        a(5, "Onderzoekers ontdekken nieuwe diepzeesoort voor de kust", "scientias", "Scientias", 130),
    )

    @Composable
    private fun Feed() {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).padding(16.dp)) {
            FlitsLogo(size = 28.dp, modifier = Modifier.padding(bottom = 16.dp))
            HeroArticleCard(articles[0], bookmarked = false, onClick = {}, onBookmark = {},
                modifier = Modifier.padding(bottom = 12.dp))
            articles.drop(1).forEach {
                ArticleCard(it, bookmarked = it.id == "3", onClick = {}, onBookmark = {})
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }

    @Test fun feedLight() = paparazzi.snapshot { FlitsTheme(ThemeMode.LIGHT) { Feed() } }
    @Test fun feedDark() = paparazzi.snapshot { FlitsTheme(ThemeMode.DARK) { Feed() } }
}
