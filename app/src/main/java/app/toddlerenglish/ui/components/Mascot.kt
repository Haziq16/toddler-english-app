package app.toddlerenglish.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import app.toddlerenglish.ui.theme.Motion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pebble's current mood. Screens call [celebrate] or [encourage]; Pebble returns to idle by itself.
 * There is deliberately no "sad" or "wrong" mood.
 */
@Stable
class MascotState(initial: Mood = Mood.Idle) {
    var mood by mutableStateOf(initial)
        private set

    /** Increments on every reaction so the same mood twice in a row still animates. */
    var pulse by mutableIntStateOf(0)
        private set

    fun celebrate() = set(Mood.Celebrate)

    fun encourage() = set(Mood.Encourage)

    fun sleep() = set(Mood.Sleep)

    fun idle() = set(Mood.Idle)

    private fun set(m: Mood) {
        mood = m
        pulse++
    }
}

@Composable
fun rememberMascotState(initial: Mood = Mood.Idle) = remember { MascotState(initial) }

/** Animated Pebble: idle bob and blink, hop and wing flap to celebrate, lean and nod to encourage. */
@Composable
fun Mascot(state: MascotState, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "pebble")
    val bob = infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(Motion.IDLE_BOB_MS, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob",
    )
    val blinkCycle = infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3_600, easing = LinearEasing)),
        label = "blink",
    )
    fun blink(): Float {
        val c = blinkCycle.value
        return if (c > 0.95f) 1f - kotlin.math.abs(c - 0.975f) / 0.025f else 0f
    }

    val hop = remember { Animatable(0f) }
    val wing = remember { Animatable(0f) }
    val tilt = remember { Animatable(0f) }

    LaunchedEffect(state.pulse) {
        when (state.mood) {
            Mood.Celebrate -> {
                launch { wing.animateTo(1f, tween(160)) }
                hop.animateTo(-1f, tween(180, easing = FastOutSlowInEasing))
                hop.animateTo(0f, spring(dampingRatio = 0.45f))
                launch { wing.animateTo(0f, tween(300)) }
                delay(Motion.CELEBRATE_MS.toLong())
                if (state.mood == Mood.Celebrate) state.idle()
            }
            Mood.Encourage -> {
                tilt.animateTo(8f, tween(200))
                tilt.animateTo(-6f, tween(260))
                tilt.animateTo(0f, spring(dampingRatio = 0.5f))
                delay(600)
                if (state.mood == Mood.Encourage) state.idle()
            }
            Mood.Sleep, Mood.Idle -> {
                wing.animateTo(0f)
                tilt.animateTo(0f)
            }
        }
    }

    val sleeping = state.mood == Mood.Sleep
    Box(
        modifier.graphicsLayer {
            val breathe = if (sleeping) 0.02f * bob.value else 0f
            translationY = (hop.value * 0.25f + (if (sleeping) 0f else bob.value * 0.03f)) * size.height
            rotationZ = tilt.value
            scaleX = 1f + breathe
            scaleY = 1f + breathe
        },
    ) {
        PebbleDrawing(
            modifier = Modifier.matchParentSize(),
            mood = state.mood,
            blink = { if (state.mood == Mood.Idle) blink() else 0f },
            wing = { wing.value },
            lean = { tilt.value * 2f + (bob.value - 0.5f) * 8f },
        )
    }
}
