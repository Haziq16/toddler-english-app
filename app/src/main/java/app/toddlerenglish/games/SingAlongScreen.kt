package app.toddlerenglish.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.toddlerenglish.audio.SongPlayer
import app.toddlerenglish.content.Game
import app.toddlerenglish.content.Song
import app.toddlerenglish.ui.components.ContentImage
import app.toddlerenglish.ui.components.Mascot
import app.toddlerenglish.ui.components.bouncyClickable
import app.toddlerenglish.ui.theme.Dimens
import app.toddlerenglish.ui.theme.Palette

/** Index of the token being sung at [positionMs], or -1 before the first word. Pure, so it is unit-tested. */
fun currentTokenIndex(song: Song, positionMs: Long): Int = song.timings.indexOfLast { it.startMs <= positionMs }

/**
 * Sing-along. Tier 1: the picture of the word being sung, big. Tier 2: plus the current line, word by word.
 * Tier 3: plus the next line, so older children can follow along.
 */
@Composable
fun SingAlongScreen(env: GameEnv, game: Game, player: SongPlayer, onSongFinished: (Song) -> Unit) {
    var playing by remember { mutableStateOf<Song?>(null) }
    var runs by remember { mutableIntStateOf(0) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) player.pause()
            if (event == Lifecycle.Event.ON_START && playing != null) player.resume()
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            player.release()
        }
    }
    LaunchedEffect(Unit) { env.say(game.phrase) }

    GameFrame(env, Palette.Sun.copy(alpha = 0.25f), showMascot = playing == null) {
        val song = playing
        if (song == null) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                contentPadding = PaddingValues(start = Dimens.SpaceM, end = Dimens.SpaceM, top = 88.dp, bottom = 110.dp),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
            ) {
                items(env.content.songs, key = { it.id }) { s ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(Palette.Paper, RoundedCornerShape(Dimens.Corner))
                            .bouncyClickable(s.title) {
                                env.audio.stop()
                                playing = s
                                runs++
                                player.play(s.audio) {
                                    onSongFinished(s)
                                    env.onRoundComplete(game.id)
                                    env.mascot.celebrate()
                                    env.audio.play(env.praiseAudio() ?: return@play)
                                    playing = null
                                }
                            }
                            .padding(Dimens.SpaceM),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ContentImage(env.content.word(s.icon)?.image.orEmpty(), null, Modifier.fillMaxWidth().aspectRatio(1.25f))
                        Text(s.title, style = MaterialTheme.typography.titleLarge, color = Palette.Ink)
                    }
                }
            }
        } else {
            NowSinging(env, song, player, runs)
        }
    }
}

@Composable
private fun NowSinging(env: GameEnv, song: Song, player: SongPlayer, run: Int) {
    var index by remember(run) { mutableIntStateOf(-1) }
    LaunchedEffect(run) {
        while (true) withFrameMillis { index = currentTokenIndex(song, player.positionMs()) }
    }
    val timing = song.timings.getOrNull(index)
    val line = timing?.line ?: 0
    // The picture shows the latest sung word that has art.
    val lastWord = song.timings.take(index + 1).reversed().firstNotNullOfOrNull { t ->
        song.lines[t.line].tokens[t.token].word?.let { env.content.word(it) }
    } ?: env.content.word(song.icon)

    val bounce = remember { Animatable(1f) }
    LaunchedEffect(lastWord?.id) {
        bounce.snapTo(1.15f)
        bounce.animateTo(1f, spring(dampingRatio = 0.4f))
    }
    val dance = remember { Animatable(0f) }
    LaunchedEffect(index) { if (index >= 0) dance.animateTo(if (index % 2 == 0) 1f else -1f, tween(180)) }

    Column(
        Modifier.fillMaxSize().padding(top = 80.dp, bottom = Dimens.SpaceL, start = Dimens.SpaceM, end = Dimens.SpaceM).testTag("singing"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Box(
            Modifier
                .size(240.dp)
                .graphicsLayer {
                    scaleX = bounce.value
                    scaleY = bounce.value
                }
                .background(Palette.Paper, RoundedCornerShape(Dimens.Corner))
                .bouncyClickable(lastWord?.text ?: song.title) { env.mascot.celebrate() }
                .padding(Dimens.SpaceL),
        ) {
            lastWord?.let { ContentImage(it.image, it.text, Modifier.fillMaxSize()) }
        }
        if (env.tier >= 2) LyricLine(song, line, if (timing?.line == line) timing.token else -1, strong = true)
        if (env.tier >= 3 && line + 1 < song.lines.size) LyricLine(song, line + 1, -1, strong = false)
        Mascot(env.mascot, Modifier.size(110.dp).graphicsLayer { rotationZ = dance.value * 8f })
    }
}

@Composable
private fun LyricLine(song: Song, line: Int, active: Int, strong: Boolean) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
        song.lines[line].tokens.forEachIndexed { i, tok ->
            val on = i == active
            Text(
                tok.text,
                style = if (strong) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                fontWeight = if (on) FontWeight.ExtraBold else FontWeight.Normal,
                color = if (strong) Palette.Ink else Palette.InkSoft,
                modifier = Modifier
                    .background(if (on) Palette.Sun else Palette.Paper.copy(alpha = 0f), RoundedCornerShape(Dimens.CornerSmall))
                    .padding(horizontal = Dimens.SpaceS),
            )
        }
    }
}
