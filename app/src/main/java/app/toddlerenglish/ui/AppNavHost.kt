package app.toddlerenglish.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.toddlerenglish.AppContainer
import app.toddlerenglish.data.Progress
import app.toddlerenglish.data.Settings
import app.toddlerenglish.parent.ParentActions
import app.toddlerenglish.parent.ParentCornerScreen
import app.toddlerenglish.parent.ParentalGateScreen
import app.toddlerenglish.ui.components.StickerCelebration
import app.toddlerenglish.ui.components.rememberMascotState
import app.toddlerenglish.ui.screens.HomeScreen
import app.toddlerenglish.ui.screens.HomeTile
import app.toddlerenglish.ui.screens.SceneScreen
import app.toddlerenglish.ui.screens.StickerBookScreen
import app.toddlerenglish.ui.theme.Palette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val SCENE = "scene/{id}"
    const val GATE = "gate"
    const val PARENT = "parent"
    const val STICKERS = "stickers"

    fun scene(id: String) = "scene/$id"
}

@Composable
fun AppNavHost(container: AppContainer, nav: NavHostController = rememberNavController()) {
    val settings by container.settings.settings.collectAsState(initial = Settings())
    val progress by container.progress.progress.collectAsState(initial = Progress())
    var celebrating by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var celebrationCount by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val activity = LocalActivity.current
    val mascot = rememberMascotState()
    val content = container.content
    val audio = container.audio

    LaunchedEffect(settings.soundOn) {
        audio.enabled = settings.soundOn
        if (!settings.soundOn) audio.stop()
    }

    // A new sticker: wait for the current word to finish, then fly the sticker in and say so.
    LaunchedEffect(Unit) {
        container.rewarder.newStickers.collect { sticker ->
            delay(900)
            celebrating = celebrationCount++ to (content.word(sticker.word)?.image ?: "")
            content.phrase("new_sticker")?.let { audio.play(it.audio) }
            mascot.celebrate()
            delay(2_200)
        }
    }

    val homeTiles = listOf(
        HomeTile(
            "stickers",
            "Stickers",
            content.word("star")?.image ?: "",
            Palette.Sun.copy(alpha = 0.45f),
            content.phrase("stickers")?.audio,
        ),
    )

    fun goHome() {
        audio.stop()
        if (!nav.popBackStack(Routes.HOME, inclusive = false)) nav.navigate(Routes.HOME)
    }

    Box(Modifier.fillMaxSize()) {
        NavHost(navController = nav, startDestination = Routes.HOME) {
            composable(Routes.HOME) {
                HomeScreen(
                    content = content,
                    audio = audio,
                    mascot = mascot,
                    greet = !container.greeted,
                    extraTiles = homeTiles,
                    onGreeted = { container.greeted = true },
                    onScene = { nav.navigate(Routes.scene(it.id)) },
                    onTile = { tile -> nav.navigate(tile.id) },
                    onParent = { nav.navigate(Routes.GATE) },
                )
            }
            composable(Routes.SCENE) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val scene = content.scenes.firstOrNull { it.id == id }
                if (scene != null) {
                    SceneScreen(
                        scene = scene,
                        content = content,
                        audio = audio,
                        mascot = mascot,
                        maxTier = settings.tier,
                        onWordHeard = { container.rewarder.wordHeard(it.id) },
                        onHome = ::goHome,
                    )
                }
            }
            composable(Routes.STICKERS) {
                StickerBookScreen(content = content, progress = progress, audio = audio, mascot = mascot, onHome = ::goHome)
            }
            composable(Routes.GATE) {
                ParentalGateScreen(
                    onSpoken = { content.phrase("grown_up")?.let { audio.play(it.audio) } },
                    onOpen = {
                        nav.navigate(Routes.PARENT) { popUpTo(Routes.HOME) }
                    },
                    onCancel = ::goHome,
                )
            }
            composable(Routes.PARENT) {
                ParentCornerScreen(
                    settings = settings,
                    actions = ParentActions(
                        onSoundOn = { scope.launch { container.settings.setSoundOn(it) } },
                        onTier = { scope.launch { container.settings.setTier(it) } },
                        onSessionLimit = { scope.launch { container.settings.setSessionLimit(it) } },
                        onResetProgress = { },
                        onBack = ::goHome,
                        onCloseApp = { activity?.finishAndRemoveTask() },
                    ),
                )
            }
        }
        celebrating?.let { (key, image) ->
            StickerCelebration(image = image, key = key) { if (celebrating?.first == key) celebrating = null }
        }
    }
}
