package app.toddlerenglish.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.toddlerenglish.ui.theme.Palette
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** A sticker: the word's art in a round badge with a little star. */
@Composable
fun StickerBadge(image: String, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size)) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Palette.Paper, CircleShape)
                .border(size * 0.05f, Palette.Sun, CircleShape)
                .padding(size * 0.14f),
        ) {
            ContentImage(image, contentDescription = null, modifier = Modifier.fillMaxSize())
        }
        Canvas(Modifier.size(size * 0.32f).align(Alignment.TopEnd)) {
            drawPath(starPath(center, this.size.minDimension / 2f), Palette.Coral)
        }
    }
}

/** An empty sticker spot: a soft dotted circle, never a "locked" or "missing" look. */
@Composable
fun StickerSpot(size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).background(Palette.Paper.copy(alpha = 0.5f), CircleShape).border(2.dp, Palette.Mist, CircleShape))
}

fun starPath(c: Offset, r: Float): Path = Path().apply {
    for (i in 0 until 10) {
        val rad = if (i % 2 == 0) r else r * 0.45f
        val a = Math.toRadians(-90.0 + i * 36.0)
        val x = c.x + (rad * cos(a)).toFloat()
        val y = c.y + (rad * sin(a)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

/** Shows a new sticker flying in, then fades it. Does not block taps underneath. */
@Composable
fun StickerCelebration(image: String, key: Any, onDone: () -> Unit) {
    val scale = remember(key) { Animatable(0f) }
    val alpha = remember(key) { Animatable(1f) }
    LaunchedEffect(key) {
        scale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 300f))
        delay(1_400)
        alpha.animateTo(0f, tween(400))
        onDone()
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        StickerBadge(
            image,
            180.dp,
            Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
                rotationZ = (1f - scale.value) * 40f
            },
        )
    }
}

/**
 * Wraps a child screen so a tap on empty space still answers with a little sparkle burst.
 * Children with their own click handling consume their taps first.
 */
@Composable
fun TapSparkles(modifier: Modifier = Modifier, onEmptyTap: () -> Unit = {}, content: @Composable () -> Unit) {
    val sparkles = remember { mutableStateListOf<Pair<Int, Offset>>() }
    var next by remember { mutableIntStateOf(0) }
    Box(modifier) {
        Box(
            Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures { pos ->
                    sparkles += next++ to pos
                    if (sparkles.size > 6) sparkles.removeAt(0)
                    onEmptyTap()
                }
            },
        )
        content()
        sparkles.forEach { (id, at) -> key(id) { SparkleBurst(at) { sparkles.removeAll { it.first == id } } } }
    }
}

@Composable
fun SparkleBurst(at: Offset, onDone: () -> Unit) {
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { t.animateTo(1f, tween(600)) }.join()
        onDone()
    }
    Canvas(Modifier.fillMaxSize()) {
        val r = 10.dp.toPx() + 40.dp.toPx() * t.value
        for (i in 0 until 8) {
            val a = Math.toRadians(i * 45.0)
            val c = Offset(at.x + (r * cos(a)).toFloat(), at.y + (r * sin(a)).toFloat())
            drawPath(starPath(c, 7.dp.toPx() * (1f - t.value)), if (i % 2 == 0) Palette.Sun else Palette.Coral)
        }
    }
}
