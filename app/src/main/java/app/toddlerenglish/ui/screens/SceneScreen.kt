package app.toddlerenglish.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.toddlerenglish.R
import app.toddlerenglish.audio.WordAudio
import app.toddlerenglish.content.Content
import app.toddlerenglish.content.SceneInfo
import app.toddlerenglish.content.Word
import app.toddlerenglish.ui.components.ContentImage
import app.toddlerenglish.ui.components.Mascot
import app.toddlerenglish.ui.components.MascotState
import app.toddlerenglish.ui.components.SparkleBurst
import app.toddlerenglish.ui.components.bouncyClickable
import app.toddlerenglish.ui.components.rememberDrawableId
import app.toddlerenglish.ui.scene.PlacedWord
import app.toddlerenglish.ui.scene.SceneLayoutEngine
import app.toddlerenglish.ui.theme.Dimens
import app.toddlerenglish.ui.theme.Motion
import app.toddlerenglish.ui.theme.Palette
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The data-driven scene. Everything on screen comes from content: background drawable, word art, positions.
 * One tap handler hit-tests the whole scene, so taps near an item still count, and a tap on empty space
 * still gets a sparkle and a happy Pebble (every tap gets a positive response).
 */
@Composable
fun SceneScreen(
    scene: SceneInfo,
    content: Content,
    audio: WordAudio,
    mascot: MascotState,
    maxTier: Int,
    onWordHeard: (Word) -> Unit,
    onHome: () -> Unit,
) {
    BackHandler(onBack = onHome)
    val items = remember(scene.id, maxTier) {
        SceneLayoutEngine.place(content.wordsInScene(scene.id), content.layouts[scene.id] ?: return@remember emptyList(), maxTier)
    }
    LaunchedEffect(items) { audio.preload(items.map { it.word.audio }) }

    // Each item animates when its counter changes.
    val pulses = remember(items) { mutableStateListOf(*Array(items.size) { 0 }) }
    var label by remember { mutableStateOf<PlacedWord?>(null) }
    var labelPulse by remember { mutableIntStateOf(0) }
    val sparkles = remember { mutableStateListOf<Sparkle>() }
    var sparkleId by remember { mutableIntStateOf(0) }

    fun hear(index: Int) {
        val item = items[index]
        audio.play(item.word.audio)
        pulses[index] = pulses[index] + 1
        label = item
        labelPulse++
        onWordHeard(item.word)
        if (pulses.sum() % 5 == 0) mascot.celebrate()
    }

    // The background fills the whole screen; items live inside the safe area so none hide under system bars.
    Box(Modifier.fillMaxSize().background(Palette.sceneTint(scene.tint)).testTag("scene_${scene.id}")) {
        val bgId = rememberDrawableId(scene.background)
        if (bgId != 0) {
            Image(painterResource(bgId), null, Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
        }
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val density = LocalDensity.current
            val wPx = with(density) { maxWidth.toPx() }
            val hPx = with(density) { maxHeight.toPx() }
            val side = minOf(maxWidth, maxHeight)
            val minRadius = with(density) { (Dimens.TouchTarget / 2).toPx() }

            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(items) {
                        detectTapGestures { pos ->
                            val hit = SceneLayoutEngine.hitTest(items, pos.x, pos.y, wPx, hPx, minRadius)
                            if (hit != null) {
                                hear(items.indexOf(hit))
                            } else {
                                sparkles += Sparkle(sparkleId++, pos)
                                if (sparkles.size > 6) sparkles.removeAt(0)
                                mascot.encourage()
                            }
                        }
                    },
            )

            items.forEachIndexed { index, item ->
                val sizeDp = (side * item.size).coerceAtLeast(Dimens.TouchTarget)
                SceneItem(
                    item = item,
                    pulse = pulses[index],
                    modifier = Modifier
                        .offset {
                            val px = sizeDp.toPx()
                            IntOffset(
                                (item.x * wPx - px / 2).coerceIn(0f, (wPx - px).coerceAtLeast(0f)).roundToInt(),
                                (item.y * hPx - px / 2).coerceIn(0f, (hPx - px).coerceAtLeast(0f)).roundToInt(),
                            )
                        }
                        .size(sizeDp)
                        .semantics {
                            contentDescription = item.word.text
                            onClick(label = "Hear ${item.word.text}") {
                                hear(index)
                                true
                            }
                        },
                )
            }

            sparkles.forEach { s -> key(s.id) { SparkleBurst(s.at) { sparkles.remove(s) } } }

            label?.let { WordLabel(it, labelPulse, wPx, hPx, side) { label = null } }

            // Way home and Pebble sit above the scene.
            Box(Modifier.fillMaxSize().padding(Dimens.SpaceS)) {
                HomeButton(onHome, Modifier.align(Alignment.TopStart))
                Mascot(
                    mascot,
                    Modifier
                        .align(Alignment.BottomStart)
                        .size(84.dp)
                        .bouncyClickable("Pebble") {
                            content.phrase("tap_and_listen")?.let { audio.play(it.audio) }
                            mascot.celebrate()
                        },
                )
            }
        }
    }
}

@Composable
private fun SceneItem(item: PlacedWord, pulse: Int, modifier: Modifier) {
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(pulse) {
        if (pulse == 0) return@LaunchedEffect
        launch {
            scale.animateTo(1.3f, tween(Motion.TAP_MS, easing = FastOutSlowInEasing))
            scale.animateTo(1f, spring(dampingRatio = 0.4f))
        }
        rotation.animateTo(-8f, tween(90))
        rotation.animateTo(8f, tween(140))
        rotation.animateTo(0f, spring(dampingRatio = 0.5f))
    }
    Box(
        modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            rotationZ = rotation.value
        },
    ) {
        if (pulse > 0) {
            // soft glow behind the highlighted item
            Box(Modifier.fillMaxSize().graphicsLayer { alpha = (scale.value - 1f) * 2.5f }.background(Palette.Sun, CircleShape))
        }
        ContentImage(item.word.image, contentDescription = null, modifier = Modifier.fillMaxSize())
    }
}

/** The word written under the item, mainly for the grown-up sitting with the child. */
@Composable
private fun WordLabel(item: PlacedWord, pulse: Int, wPx: Float, hPx: Float, side: Dp, onDone: () -> Unit) {
    LaunchedEffect(pulse) {
        delay(1_800)
        onDone()
    }
    val density = LocalDensity.current
    val below = with(density) { (side * item.size / 2).toPx() } + with(density) { 6.dp.toPx() }
    val y = (item.y * hPx + below).coerceAtMost(hPx - with(density) { 48.dp.toPx() })
    Text(
        item.word.text,
        style = MaterialTheme.typography.titleLarge,
        color = Palette.Ink,
        modifier = Modifier
            .offset { IntOffset((item.x * wPx).roundToInt() - 60.dp.roundToPx(), y.roundToInt()) }
            .size(width = 120.dp, height = 40.dp)
            .background(Palette.Paper.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
            .padding(top = 6.dp),
        textAlign = TextAlign.Center,
    )
}

private data class Sparkle(val id: Int, val at: Offset)

@Composable
fun HomeButton(onHome: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(Dimens.TouchTarget)
            .background(Palette.Paper.copy(alpha = 0.85f), CircleShape)
            .bouncyClickable("Home", onClick = onHome)
            .testTag("home_button"),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(R.drawable.ic_home), contentDescription = "Home", modifier = Modifier.size(36.dp))
    }
}
