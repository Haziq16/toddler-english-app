package app.toddlerenglish.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.toddlerenglish.AppContainer
import app.toddlerenglish.data.Settings
import app.toddlerenglish.parent.ParentActions
import app.toddlerenglish.parent.ParentCornerScreen
import app.toddlerenglish.parent.ParentalGateScreen
import app.toddlerenglish.ui.components.rememberMascotState
import app.toddlerenglish.ui.screens.HomeScreen
import app.toddlerenglish.ui.screens.SceneScreen
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val SCENE = "scene/{id}"
    const val GATE = "gate"
    const val PARENT = "parent"

    fun scene(id: String) = "scene/$id"
}

@Composable
fun AppNavHost(container: AppContainer, nav: NavHostController = rememberNavController()) {
    val settings by container.settings.settings.collectAsState(initial = Settings())
    val scope = rememberCoroutineScope()
    val activity = LocalActivity.current
    val mascot = rememberMascotState()
    val content = container.content
    val audio = container.audio

    LaunchedEffect(settings.soundOn) {
        audio.enabled = settings.soundOn
        if (!settings.soundOn) audio.stop()
    }

    fun goHome() {
        audio.stop()
        if (!nav.popBackStack(Routes.HOME, inclusive = false)) nav.navigate(Routes.HOME)
    }

    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                content = content,
                audio = audio,
                mascot = mascot,
                greet = !container.greeted,
                extraTiles = emptyList(),
                onGreeted = { container.greeted = true },
                onScene = { nav.navigate(Routes.scene(it.id)) },
                onTile = { },
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
                    onWordHeard = { },
                    onHome = ::goHome,
                )
            }
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
}
