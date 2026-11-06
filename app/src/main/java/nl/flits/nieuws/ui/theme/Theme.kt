package nl.flits.nieuws.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import nl.flits.nieuws.data.ThemeMode

val FlitsRed = Color(0xFFE5132B)
val FlitsRedDark = Color(0xFFA50D1F)
val FlitsRedLight = Color(0xFFFF4B5C)

private val LightColors = lightColorScheme(
    primary = FlitsRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD8),
    onPrimaryContainer = Color(0xFF410006),
    secondary = Color(0xFF775654),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE9E7),
    onSecondaryContainer = Color(0xFF2C1513),
    tertiary = Color(0xFF1F1A1B),
    background = Color(0xFFFFFBFA),
    onBackground = Color(0xFF1C1516),
    surface = Color(0xFFFFFBFA),
    onSurface = Color(0xFF1C1516),
    surfaceVariant = Color(0xFFF5EDEC),
    onSurfaceVariant = Color(0xFF5B4F4E),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFF4F3),
    surfaceContainer = Color(0xFFFBEEED),
    surfaceContainerHigh = Color(0xFFF5E8E7),
    surfaceContainerHighest = Color(0xFFEFE2E1),
    outline = Color(0xFF8E7F7E),
    outlineVariant = Color(0xFFE3D6D5),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF5A68),
    onPrimary = Color(0xFF5F0010),
    primaryContainer = Color(0xFF93001C),
    onPrimaryContainer = Color(0xFFFFDAD8),
    secondary = Color(0xFFE7BDB9),
    onSecondary = Color(0xFF442927),
    secondaryContainer = Color(0xFF3A2523),
    onSecondaryContainer = Color(0xFFFFDAD7),
    tertiary = Color(0xFFF2E6E5),
    background = Color(0xFF121014),
    onBackground = Color(0xFFEDE0DF),
    surface = Color(0xFF121014),
    onSurface = Color(0xFFEDE0DF),
    surfaceVariant = Color(0xFF2A2427),
    onSurfaceVariant = Color(0xFFD5C4C3),
    surfaceContainerLowest = Color(0xFF0C0A0D),
    surfaceContainerLow = Color(0xFF1A1619),
    surfaceContainer = Color(0xFF1F1A1D),
    surfaceContainerHigh = Color(0xFF2A2428),
    surfaceContainerHighest = Color(0xFF352E32),
    outline = Color(0xFF9F8D8C),
    outlineVariant = Color(0xFF3F3537),
)

private val base = Typography()
private val FlitsTypography = Typography(
    displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
    headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.25).sp),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold, lineHeight = 28.sp),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 22.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp),
    bodyLarge = base.bodyLarge.copy(lineHeight = 26.sp),
)

@Composable
fun FlitsTheme(mode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = FlitsTypography,
        content = content,
    )
}
