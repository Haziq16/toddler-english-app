package app.toddlerenglish.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.toddlerenglish.content.Game
import app.toddlerenglish.content.Word
import app.toddlerenglish.ui.components.ContentImage
import app.toddlerenglish.ui.components.Mascot
import app.toddlerenglish.ui.components.bouncyClickable
import app.toddlerenglish.ui.theme.Dimens
import app.toddlerenglish.ui.theme.Palette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** A big picture card. [pulse] bounces it (found); [wiggle] shakes it gently (named, not the one asked for). */
@Composable
fun ChoiceCard(word: Word, size: Dp, pulse: Int, wiggle: Int, glow: Boolean, onTap: () -> Unit, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(1f) }
    val rot = remember { Animatable(0f) }
    LaunchedEffect(pulse) {
        if (pulse > 0) {
            scale.animateTo(1.2f, tween(160))
            scale.animateTo(1f, spring(dampingRatio = 0.4f))
        }
    }
    LaunchedEffect(wiggle) {
        if (wiggle > 0) {
            rot.animateTo(-6f, tween(80))
            rot.animateTo(6f, tween(120))
            rot.animateTo(0f, spring(dampingRatio = 0.5f))
        }
    }
    Box(
        modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                rotationZ = rot.value
            }
            .background(if (glow) Palette.Sun else Palette.Paper, RoundedCornerShape(Dimens.Corner))
            .bouncyClickable(word.text, onClick = onTap)
            .semantics { contentDescription = word.text }
            .testTag("choice_${word.id}")
            .padding(size * 0.1f),
    ) {
        ContentImage(word.image, null, Modifier.fillMaxSize())
    }
}

/** Lays out 2, 3 or 4 cards: one row for 2, a 2 x 2 grid for 3-4. */
@Composable
fun ChoiceGrid(words: List<Word>, maxCard: Dp, card: @Composable (Word, Dp) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val perRow = 2
        val cardSize = minOf(maxCard, (maxWidth - Dimens.SpaceM * (perRow + 1)) / perRow)
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        ) {
            words.chunked(perRow).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) { row.forEach { card(it, cardSize) } }
            }
        }
    }
}

/** "Find it": Pebble names a picture, the child finds it. Other pictures are simply named. */
@Composable
fun MatchScreen(env: GameEnv, game: Game) {
    val tier = env.tier
    val choices = TierRules.matchChoices(tier)
    var deck by remember { mutableStateOf(game.pickDeck(env.content, tier, choices, env.random) ?: game.decks.keys.first()) }
    val round = remember(deck) { FindRound(game.deckWords(env.content, deck, tier), choices, TierRules.FINDS_PER_ROUND, env.random) }
    var turn by remember { mutableIntStateOf(0) }
    val pulses = remember { mutableStateMapOf<String, Int>() }
    val wiggles = remember { mutableStateMapOf<String, Int>() }
    var glowing by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun ask() {
        val prompt = listOfNotNull(env.phraseAudio("find_the"), round.target.audio)
        env.audio.playSequence(prompt)
    }
    LaunchedEffect(deck, turn) {
        env.audio.preload(round.options.map { it.audio })
        if (turn == 0) {
            env.say(game.phrase)
            delay(env.audio.durationMs(env.phraseAudio(game.phrase).orEmpty()) + 200)
        }
        ask()
    }

    GameFrame(env, Palette.Lilac.copy(alpha = 0.35f), showMascot = false) {
        Column(
            Modifier.fillMaxSize().padding(top = 80.dp, bottom = Dimens.SpaceL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            Mascot(env.mascot, Modifier.size(120.dp).bouncyClickable("Hear it again") { ask() })
            ChoiceGrid(round.options, 220.dp) { word, size ->
                ChoiceCard(word, size, pulses[word.id] ?: 0, wiggles[word.id] ?: 0, glowing == word.id, onTap = {
                    if (busy) {
                        env.audio.play(word.audio)
                        return@ChoiceCard
                    }
                    when (val r = round.tap(word)) {
                        is TapResult.Found -> {
                            busy = true
                            pulses[word.id] = (pulses[word.id] ?: 0) + 1
                            glowing = word.id
                            env.mascot.celebrate()
                            val total = env.audio.playSequence(listOfNotNull(word.audio, env.praiseAudio()))
                            if (r.roundComplete) env.onRoundComplete(game.id)
                            scope.launch {
                                delay(total + 150)
                                glowing = null
                                busy = false
                                if (r.roundComplete) deck = game.pickDeck(env.content, tier, choices, env.random) ?: deck
                                turn++
                            }
                        }
                        is TapResult.Named -> {
                            wiggles[word.id] = (wiggles[word.id] ?: 0) + 1
                            env.mascot.encourage()
                            env.audio.playSequence(listOfNotNull(word.audio, env.phraseAudio("find_the"), round.target.audio))
                        }
                    }
                })
            }
        }
    }
}

/** Feed Pebble: tap a food and it flies into Pebble's beak. From tier 2 Pebble asks for one food by name. */
@Composable
fun FeedScreen(env: GameEnv, game: Game) {
    val tier = env.tier
    val choices = TierRules.feedChoices(tier)
    val asks = TierRules.feedAsksForFood(tier)
    val deck = remember { game.pickDeck(env.content, tier, choices, env.random) ?: game.decks.keys.first() }
    val round = remember { FindRound(game.deckWords(env.content, deck, tier), choices, TierRules.FEEDS_PER_ROUND, env.random) }
    var turn by remember { mutableIntStateOf(0) }
    val wiggles = remember { mutableStateMapOf<String, Int>() }
    val itemCenters = remember { mutableStateMapOf<String, Offset>() }
    var mouth by remember { mutableStateOf(Offset.Zero) }
    var flying by remember { mutableStateOf<Word?>(null) }
    val flight = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    fun ask() {
        if (asks) env.audio.playSequence(listOfNotNull(env.phraseAudio("find_the"), round.target.audio)) else env.say("feed_more")
    }
    LaunchedEffect(turn) {
        env.audio.preload(round.options.map { it.audio })
        if (turn == 0) {
            env.say(game.phrase)
            delay(env.audio.durationMs(env.phraseAudio(game.phrase).orEmpty()) + 200)
            if (asks) ask()
        } else {
            ask()
        }
    }

    fun eat(word: Word, result: TapResult.Found) {
        flying = word
        scope.launch {
            flight.snapTo(0f)
            flight.animateTo(1f, tween(550))
            env.mascot.celebrate()
            val total = env.audio.playSequence(listOfNotNull(word.audio, env.phraseAudio("feed_yum")))
            if (result.roundComplete) env.onRoundComplete(game.id)
            delay(total)
            flying = null
            turn++
        }
    }

    GameFrame(env, Palette.Blush.copy(alpha = 0.35f), showMascot = false) {
        Column(
            Modifier.fillMaxSize().padding(top = 80.dp, bottom = Dimens.SpaceL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            Box(
                Modifier
                    .size(200.dp)
                    .onGloballyPositioned { mouth = it.boundsInRoot().center }
                    .bouncyClickable("Pebble") { ask() },
            ) {
                Box(Modifier.size(200.dp).background(Palette.Paper.copy(alpha = 0.5f), CircleShape))
                Mascot(env.mascot, Modifier.size(200.dp))
            }
            ChoiceGrid(round.options, 180.dp) { word, size ->
                val isFlying = flying?.id == word.id
                Box(
                    Modifier
                        .onGloballyPositioned { itemCenters[word.id] = it.boundsInRoot().center }
                        .graphicsLayer {
                            if (isFlying) {
                                val from = itemCenters[word.id] ?: Offset.Zero
                                translationX = (mouth.x - from.x) * flight.value
                                translationY = (mouth.y - from.y) * flight.value
                                val s = 1f - 0.8f * flight.value
                                scaleX = s
                                scaleY = s
                                alpha = 1f - 0.6f * flight.value
                            }
                        },
                ) {
                    ChoiceCard(word, size, 0, wiggles[word.id] ?: 0, false, onTap = {
                        if (flying != null) {
                            env.audio.play(word.audio)
                            return@ChoiceCard
                        }
                        if (!asks) {
                            eat(word, round.acceptAny(word))
                        } else {
                            when (val r = round.tap(word)) {
                                is TapResult.Found -> eat(word, r)
                                is TapResult.Named -> {
                                    wiggles[word.id] = (wiggles[word.id] ?: 0) + 1
                                    env.mascot.encourage()
                                    env.audio.playSequence(listOfNotNull(word.audio, env.phraseAudio("find_the"), round.target.audio))
                                }
                            }
                        }
                    })
                }
            }
        }
    }
}
