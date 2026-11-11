package app.toddlerenglish.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design tokens. Every colour, size and type style in the UI comes from here.
 * Calm, warm palette with soft contrast for art and high contrast for text.
 */
object Palette {
    val Cream = Color(0xFFFFF8EC)
    val Paper = Color(0xFFFFFFFF)
    val Mist = Color(0xFFEAF4EF)
    val Ink = Color(0xFF2E3A3F)
    val InkSoft = Color(0xFF4F5D63)

    val Leaf = Color(0xFF8CC84B)
    val Sun = Color(0xFFFFD166)
    val Sky = Color(0xFF9ED8F0)
    val Coral = Color(0xFFF4A259)
    val Blush = Color(0xFFF6B8B0)
    val Lilac = Color(0xFFC7B8EA)

    // Scenes and games
    val SceneHalo = Color(0x8CFFFFFF)
    val BubbleFill = Color(0x66FFFFFF)
    val BubbleEdge = Color(0xFF9ED8F0)

    // Mascot
    val PebbleBody = Color(0xFF5FB49C)
    val PebbleBelly = Color(0xFFFFF4DC)
    val PebbleBeak = Color(0xFFF4A259)
    val PebbleLeaf = Color(0xFF8CC84B)

    // Scene tints, keyed by the scene's `tint` field in content.
    val sceneTints = mapOf(
        "home" to Color(0xFFFFE8C7),
        "farm" to Color(0xFFDFF0C8),
        "food" to Color(0xFFFFDCD3),
        "bedtime" to Color(0xFFD9D4F2),
        "outdoors" to Color(0xFFCFEAF7),
    )

    fun sceneTint(key: String): Color = sceneTints[key] ?: Mist
}

object Dimens {
    /** Minimum size of anything a child can touch (D-005). */
    val TouchTarget = 64.dp

    /** Home-screen scene buttons and game cards. */
    val BigTarget = 120.dp
    val SpaceXs = 4.dp
    val SpaceS = 8.dp
    val SpaceM = 16.dp
    val SpaceL = 24.dp
    val SpaceXl = 32.dp
    val Corner = 24.dp
    val CornerSmall = 12.dp
}

object Motion {
    const val TAP_MS = 180
    const val HIGHLIGHT_MS = 700
    const val CELEBRATE_MS = 1200
    const val IDLE_BOB_MS = 1800
}

private val Rounded = FontFamily.SansSerif

val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 56.sp),
    headlineLarge = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    bodyLarge = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    labelLarge = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 22.sp),
)
