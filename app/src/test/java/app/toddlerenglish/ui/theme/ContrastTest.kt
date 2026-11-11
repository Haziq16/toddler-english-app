package app.toddlerenglish.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import org.junit.Assert.assertTrue
import org.junit.Test

/** WCAG contrast for every text colour on every surface it is drawn on (S7 contrast audit). */
class ContrastTest {
    private fun channel(c: Float) = if (c <= 0.03928f) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)

    private fun luminance(c: Color) = 0.2126 * channel(c.red) + 0.7152 * channel(c.green) + 0.0722 * channel(c.blue)

    /** Flattens a translucent colour onto [under]. */
    private fun Color.over(under: Color) = Color(
        red = red * alpha + under.red * (1 - alpha),
        green = green * alpha + under.green * (1 - alpha),
        blue = blue * alpha + under.blue * (1 - alpha),
    )

    fun ratio(a: Color, b: Color): Double {
        val (l1, l2) = listOf(luminance(a), luminance(b)).sortedDescending()
        return (l1 + 0.05) / (l2 + 0.05)
    }

    private fun assertAA(name: String, text: Color, bg: Color, min: Double = 4.5) {
        val r = ratio(text, bg)
        assertTrue("$name contrast ${"%.2f".format(r)} < $min", r >= min)
    }

    @Test
    fun bodyTextMeetsAA() {
        assertAA("Ink on Cream", Palette.Ink, Palette.Cream)
        assertAA("Ink on Paper", Palette.Ink, Palette.Paper)
        assertAA("Ink on Mist", Palette.Ink, Palette.Mist)
        assertAA("InkSoft on Paper", Palette.InkSoft, Palette.Paper)
        assertAA("InkSoft on Mist", Palette.InkSoft, Palette.Mist)
        assertAA("InkSoft on Cream", Palette.InkSoft, Palette.Cream)
    }

    @Test
    fun tileTitlesMeetAA() {
        Palette.sceneTints.forEach { (id, tint) -> assertAA("Ink on $id tile", Palette.Ink, tint) }
        listOf(Palette.Sky.copy(alpha = 0.6f), Palette.Lilac.copy(alpha = 0.6f), Palette.Sun.copy(alpha = 0.45f)).forEach {
            assertAA("Ink on home tile", Palette.Ink, it.over(Palette.Cream))
        }
    }

    @Test
    fun buttonsMeetAA() {
        assertAA("Ink on Pebble button", Palette.Ink, Palette.PebbleBody)
        assertAA("Controls on Paper (non-text, 3:1)", Palette.Forest, Palette.Paper, min = 3.0)
        assertAA("White on Forest", Palette.Paper, Palette.Forest)
        assertAA("Ink on highlighted lyric", Palette.Ink, Palette.Sun)
        assertAA("Ink on word label", Palette.Ink, Palette.Paper.copy(alpha = 0.9f).over(Palette.Leaf))
    }
}
