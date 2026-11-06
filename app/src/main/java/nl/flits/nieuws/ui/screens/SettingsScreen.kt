package nl.flits.nieuws.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.flits.nieuws.BuildConfig
import nl.flits.nieuws.data.Category
import nl.flits.nieuws.data.Sources
import nl.flits.nieuws.data.ThemeMode
import nl.flits.nieuws.ui.NewsViewModel
import nl.flits.nieuws.ui.components.FlitsLogo
import nl.flits.nieuws.util.openArticle

private const val PRIVACY_URL = "https://robindrabbels.github.io/nieuwsapp/privacy.html"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(vm: NewsViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("Instellingen", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 4.dp))
        }
        item {
            Section("Weergave") {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    val options = listOf(ThemeMode.SYSTEM to "Systeem", ThemeMode.LIGHT to "Licht", ThemeMode.DARK to "Donker")
                    options.forEachIndexed { i, (mode, label) ->
                        SegmentedButton(
                            selected = settings.themeMode == mode,
                            onClick = { vm.setTheme(mode) },
                            shape = SegmentedButtonDefaults.itemShape(i, options.size),
                        ) { Text(label) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                        Text("Artikelen in de app openen", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Uit: openen in je standaardbrowser",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = settings.openInApp, onCheckedChange = vm::setOpenInApp)
                }
            }
        }
        item {
            Section("Categorieën", "Kies welke rubrieken je in de tabbalk ziet.") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Category.entries.filter { it != Category.TOP }.forEach { cat ->
                        val on = cat.id !in settings.hiddenCategories
                        FilterChip(
                            selected = on,
                            onClick = { vm.toggleCategory(cat.id) },
                            label = { Text(cat.label) },
                            leadingIcon = if (on) {{ Icon(Icons.Filled.Check, null) }} else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
            }
        }
        item {
            Section("Nieuwsbronnen", "Zet bronnen uit die je niet wilt zien.") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Sources.all.forEach { src ->
                        val on = src.id !in settings.disabledSources
                        FilterChip(
                            selected = on,
                            onClick = { vm.toggleSource(src.id) },
                            label = { Text(src.name) },
                            leadingIcon = if (on) {{ Icon(Icons.Filled.Check, null) }} else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
            }
        }
        item {
            Section("Over Flits") {
                FlitsLogo(size = 36.dp)
                Spacer(Modifier.height(10.dp))
                Text(
                    "Flits bundelt de headlines van de grootste Nederlandse en internationale nieuwssites via hun " +
                        "openbare RSS-feeds. Artikelen worden geopend op de website van de uitgever; alle rechten " +
                        "op de inhoud liggen bij de betreffende bronnen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Privacybeleid",
                    color = primary,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .clickable { openArticle(context, PRIVACY_URL, settings.openInApp, primary.toArgb()) }
                        .padding(vertical = 6.dp),
                )
                Text(
                    "Versie ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Section(title: String, subtitle: String? = null, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        androidx.compose.foundation.layout.Column(Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
