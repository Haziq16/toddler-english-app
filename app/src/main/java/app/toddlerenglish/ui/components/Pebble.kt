package app.toddlerenglish.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import app.toddlerenglish.ui.theme.Palette

/** How Pebble looks. Animation between moods lives in `Mascot` (S4). */
enum class Mood { Idle, Celebrate, Encourage, Sleep }

/**
 * Pebble, the original mascot: a round sea-green seed-bird with a leaf sprout (docs/MASCOT.md).
 * Drawn on a 100x100 grid scaled to the canvas, so it stays crisp at any size.
 *
 * @param blink 0 = eyes open, 1 = closed
 * @param wing  -1..1, wing lift (1 = wings up for celebrating)
 * @param lean  degrees the leaf sprout leans
 */
@Composable
fun PebbleDrawing(modifier: Modifier = Modifier, mood: Mood = Mood.Idle, blink: Float = 0f, wing: Float = 0f, lean: Float = 0f) {
    Canvas(modifier) {
        val s = size.minDimension / 100f
        val ox = (size.width - 100f * s) / 2f
        val oy = (size.height - 100f * s) / 2f
        fun p(x: Float, y: Float) = Offset(ox + x * s, oy + y * s)
        drawPebble(::p, s, mood, blink, wing, lean)
    }
}

private fun DrawScope.drawPebble(p: (Float, Float) -> Offset, s: Float, mood: Mood, blink: Float, wing: Float, lean: Float) {
    val ink = Palette.Ink
    // sprout
    rotate(lean, pivot = p(50f, 24f)) {
        drawLine(Color(0xFF6FA83A), p(50f, 26f), p(50f, 15f), strokeWidth = 3f * s, cap = StrokeCap.Round)
        val leaf = Path().apply {
            val a = p(50f, 16f)
            moveTo(a.x, a.y)
            val c1 = p(50f, 6f)
            val c2 = p(60f, 2f)
            val e = p(68f, 4f)
            cubicTo(c1.x, c1.y, c2.x, c2.y, e.x, e.y)
            val d1 = p(66f, 13f)
            val d2 = p(58f, 17f)
            cubicTo(d1.x, d1.y, d2.x, d2.y, a.x, a.y)
            close()
        }
        drawPath(leaf, Palette.PebbleLeaf)
    }
    // feet
    drawOval(Palette.PebbleBeak, topLeft = p(32f, 86f), size = Size(14f * s, 8f * s))
    drawOval(Palette.PebbleBeak, topLeft = p(54f, 86f), size = Size(14f * s, 8f * s))
    // wings (behind body)
    val lift = wing * 26f
    rotate(-20f - lift, pivot = p(22f, 58f)) {
        drawOval(Color(0xFF4E9C85), topLeft = p(10f, 50f), size = Size(18f * s, 26f * s))
    }
    rotate(20f + lift, pivot = p(78f, 58f)) {
        drawOval(Color(0xFF4E9C85), topLeft = p(72f, 50f), size = Size(18f * s, 26f * s))
    }
    // body and belly
    drawCircle(Palette.PebbleBody, radius = 36f * s, center = p(50f, 56f))
    drawOval(Palette.PebbleBelly, topLeft = p(29f, 56f), size = Size(42f * s, 34f * s))
    // cheeks
    drawCircle(Palette.Blush, radius = 4.5f * s, center = p(27f, 58f))
    drawCircle(Palette.Blush, radius = 4.5f * s, center = p(73f, 58f))
    // eyes
    val happy = mood == Mood.Celebrate
    val closed = mood == Mood.Sleep || blink >= 0.95f
    listOf(38f, 62f).forEach { x ->
        when {
            closed -> drawArc(
                ink,
                0f,
                180f,
                false,
                topLeft = p(x - 5f, 42f),
                size = Size(10f * s, 7f * s),
                style = Stroke(width = 2.6f * s, cap = StrokeCap.Round),
            )
            happy -> drawArc(
                ink,
                180f,
                180f,
                false,
                topLeft = p(x - 5f, 43f),
                size = Size(10f * s, 8f * s),
                style = Stroke(width = 2.8f * s, cap = StrokeCap.Round),
            )
            else -> {
                val h = 11f * (1f - blink).coerceAtLeast(0.15f)
                drawOval(ink, topLeft = p(x - 4.5f, 46f - h / 2f), size = Size(9f * s, h * s))
                if (blink < 0.5f) drawCircle(Color.White, radius = 1.6f * s, center = p(x + 1.5f, 43.5f))
            }
        }
    }
    // beak: open when celebrating or encouraging
    val open = mood == Mood.Celebrate || mood == Mood.Encourage
    val beak = Path().apply {
        val a = p(44f, 54f)
        val b = p(56f, 54f)
        val c = p(50f, if (open) 63f else 60f)
        moveTo(a.x, a.y)
        lineTo(b.x, b.y)
        lineTo(c.x, c.y)
        close()
    }
    drawPath(beak, Palette.PebbleBeak)
    if (open) drawLine(Color(0xFFD9824B), p(45.5f, 56.5f), p(54.5f, 56.5f), strokeWidth = 1.6f * s)
}
