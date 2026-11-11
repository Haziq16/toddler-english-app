package app.toddlerenglish.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.toddlerenglish.content.Game
import app.toddlerenglish.content.Word
import app.toddlerenglish.ui.components.ContentImage
import app.toddlerenglish.ui.components.SparkleBurst
import app.toddlerenglish.ui.components.bouncyClickable
import app.toddlerenglish.ui.theme.Palette
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay

private class Bubble(val id: Int, val word: Word, val x: Float, val bornAt: Long, val riseMs: Long, val wobble: Float)

/** Pop the bubbles: pictures float up in bubbles; each pop says the word. Bubbles that float away just come back. */
@Composable
fun BubblesScreen(env: GameEnv, game: Game) {
    val tier = env.tier
    val count = TierRules.bubblesOnScreen(tier)
    var deck by remember { mutableStateOf(game.pickDeck(env.content, tier, count, env.random) ?: game.decks.keys.first()) }
    val round = remember(deck) { BubblesRound(game.deckWords(env.content, deck, tier), env.random) }
    val bubbles = remember(deck) { mutableStateListOf<Bubble>() }
    var nextId by remember { mutableIntStateOf(0) }
    var now by remember { mutableLongStateOf(0L) }
    var popped by remember { mutableStateOf<Pair<Int, Word>?>(null) }
    val bursts = remember { mutableStateListOf<Pair<Int, Offset>>() }
    val riseMs = (TierRules.bubbleRiseSeconds(tier) * 1000).toLong()

    LaunchedEffect(deck) { env.audio.preload(game.deckWords(env.content, deck, tier).map { it.audio }) }
    LaunchedEffect(Unit) { env.say(game.phrase) }

    fun spawn(at: Long, stagger: Int) {
        val word = round.nextWord(bubbles.map { it.word.id })
        val lane = (bubbles.size + stagger) % count
        val x = (lane + 0.5f) / count + (env.random.nextFloat() - 0.5f) * 0.08f
        bubbles += Bubble(nextId++, word, x.coerceIn(0.1f, 0.9f), at - stagger * riseMs / count, riseMs, env.random.nextFloat() * 6.28f)
    }

    LaunchedEffect(deck) {
        while (true) {
            withFrameMillis { t ->
                now = t
                if (bubbles.isEmpty()) repeat(count) { spawn(t, it) }
                bubbles.removeAll { t - it.bornAt > it.riseMs }
                while (bubbles.size < count) spawn(t, 0)
            }
        }
    }

    GameFrame(env, Palette.Sky.copy(alpha = 0.5f)) {
        BoxWithConstraints(Modifier.fillMaxSize().testTag("bubbles")) {
            val density = LocalDensity.current
            val w = with(density) { maxWidth.toPx() }
            val h = with(density) { maxHeight.toPx() }
            val sizeDp = when (tier) {
                1 -> 150.dp
                2 -> 130.dp
                else -> 115.dp
            }
            val sizePx = with(density) { sizeDp.toPx() }
            bubbles.forEach { b ->
                key(b.id) {
                    val p = ((now - b.bornAt).toFloat() / b.riseMs).coerceIn(0f, 1f)
                    val cx = (b.x * w + sin(p * 9f + b.wobble) * sizePx * 0.15f).coerceIn(sizePx / 2, w - sizePx / 2)
                    val cy = h + sizePx / 2 - p * (h + sizePx)
                    Box(
                        Modifier
                            .offset { IntOffset((cx - sizePx / 2).roundToInt(), (cy - sizePx / 2).roundToInt()) }
                            .size(sizeDp)
                            .background(Palette.BubbleFill, CircleShape)
                            .border(3.dp, Palette.BubbleEdge, CircleShape)
                            .bouncyClickable("Pop ${b.word.text}") {
                                bubbles.remove(b)
                                bursts += b.id to Offset(cx, cy)
                                env.audio.play(b.word.audio)
                                popped = b.id to b.word
                                if (round.pop()) {
                                    env.onRoundComplete(game.id)
                                    env.mascot.celebrate()
                                    deck = game.pickDeck(env.content, tier, count, env.random) ?: deck
                                } else if (round.pops % 3 == 0) {
                                    env.mascot.celebrate()
                                }
                            }
                            .semantics { contentDescription = b.word.text }
                            .padding(sizeDp * 0.18f),
                    ) {
                        ContentImage(b.word.image, null, Modifier.fillMaxSize())
                    }
                }
            }
            bursts.forEach { (id, at) -> key(id) { SparkleBurst(at) { bursts.removeAll { it.first == id } } } }
            popped?.let { (id, word) -> PoppedWord(id, word) { if (popped?.first == id) popped = null } }
        }
    }
}

/** After a pop, the picture shows big in the middle for a moment while the word is spoken. */
@Composable
private fun PoppedWord(id: Int, word: Word, onDone: () -> Unit) {
    val scale = remember(id) { Animatable(0.3f) }
    val alpha = remember(id) { Animatable(1f) }
    LaunchedEffect(id) {
        scale.animateTo(1f, spring(dampingRatio = 0.5f))
        delay(700)
        alpha.animateTo(0f, tween(300))
        onDone()
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ContentImage(
            word.image,
            null,
            Modifier
                .size(200.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                },
        )
    }
}
