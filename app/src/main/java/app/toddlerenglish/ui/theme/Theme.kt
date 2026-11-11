package app.toddlerenglish.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Palette.Forest,
    onPrimary = Palette.Paper,
    secondary = Palette.Sun,
    onSecondary = Palette.Ink,
    tertiary = Palette.Sky,
    background = Palette.Cream,
    onBackground = Palette.Ink,
    surface = Palette.Paper,
    onSurface = Palette.Ink,
    surfaceVariant = Palette.Mist,
    onSurfaceVariant = Palette.InkSoft,
)

/**
 * The app always uses its calm light palette, so scenes look the same for every child.
 * Dark mode is deliberately not followed (D-010).
 */
@Composable
fun ToddlerEnglishTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content,
    )
}
