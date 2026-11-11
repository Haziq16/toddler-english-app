package app.toddlerenglish.games

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.toddlerenglish.audio.WordAudio
import app.toddlerenglish.content.Content
import app.toddlerenglish.content.Game
import app.toddlerenglish.ui.components.ContentImage
import app.toddlerenglish.ui.components.Mascot
import app.toddlerenglish.ui.components.MascotState
import app.toddlerenglish.ui.components.TapSparkles
import app.toddlerenglish.ui.components.bouncyClickable
import app.toddlerenglish.ui.screens.HomeButton
import app.toddlerenglish.ui.theme.Dimens
import app.toddlerenglish.ui.theme.Palette
import kotlin.random.Random

/** Everything a game screen needs. */
class GameEnv(
    val content: Content,
    val audio: WordAudio,
    val mascot: MascotState,
    val tier: Int,
    val onRoundComplete: (gameId: String) -> Unit,
    val onHome: () -> Unit,
    val random: Random = Random.Default,
) {
    fun phraseAudio(id: String): String? = content.phrase(id)?.audio

    fun say(id: String) {
        phraseAudio(id)?.let(audio::play)
    }

    fun praiseAudio(): String? = phraseAudio("praise_${random.nextInt(1, 6)}")
}

/** Common frame for games: sparkles on empty taps, home button, and an optional Pebble corner. */
@Composable
fun GameFrame(env: GameEnv, tint: Color, showMascot: Boolean = true, content: @Composable BoxScope.() -> Unit) {
    BackHandler(onBack = env.onHome)
    TapSparkles(Modifier.fillMaxSize().background(tint), onEmptyTap = { env.mascot.encourage() }) {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            content()
            HomeButton(env.onHome, Modifier.align(Alignment.TopStart).padding(Dimens.SpaceS))
            if (showMascot) {
                Mascot(
                    env.mascot,
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(Dimens.SpaceS)
                        .size(84.dp)
                        .bouncyClickable("Pebble") { env.mascot.celebrate() },
                )
            }
        }
    }
}

/** The games menu: one big tile per game from games.json. */
@Composable
fun GamesMenuScreen(env: GameEnv, onGame: (Game) -> Unit) {
    GameFrame(env, Palette.Mist, showMascot = true) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(start = Dimens.SpaceM, end = Dimens.SpaceM, top = 88.dp, bottom = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        ) {
            items(env.content.games, key = { it.id }) { game ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Palette.Paper, RoundedCornerShape(Dimens.Corner))
                        .bouncyClickable(game.title) {
                            env.say(game.phrase)
                            onGame(game)
                        }
                        .semantics { contentDescription = game.title }
                        .padding(Dimens.SpaceM),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val icon = env.content.word(game.icon)?.image.orEmpty()
                    ContentImage(icon, null, Modifier.fillMaxWidth().aspectRatio(1.25f))
                    Text(game.title, style = MaterialTheme.typography.titleLarge, color = Palette.Ink)
                }
            }
        }
    }
}
