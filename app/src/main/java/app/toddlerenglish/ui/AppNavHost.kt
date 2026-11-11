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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.toddlerenglish.AppContainer
import app.toddlerenglish.audio.SongPlayer
import app.toddlerenglish.data.Progress
import app.toddlerenglish.data.Settings
import app.toddlerenglish.games.BubblesScreen
import app.toddlerenglish.games.FeedScreen
import app.toddlerenglish.games.GameEnv
import app.toddlerenglish.games.GamesMenuScreen
import app.toddlerenglish.games.MatchScreen
import app.toddlerenglish.games.SingAlongScreen
import app.toddlerenglish.parent.ParentActions
import app.toddlerenglish.parent.ParentCornerScreen
import app.toddlerenglish.parent.ParentalGateScreen
import app.toddlerenglish.parent.SessionTimer
import app.toddlerenglish.ui.components.StickerCelebration
import app.toddlerenglish.ui.components.rememberMascotState
import app.toddlerenglish.ui.screens.HomeScreen
import app.toddlerenglish.ui.screens.HomeTile
import app.toddlerenglish.ui.screens.RestScreen
import app.toddlerenglish.ui.screens.SceneScreen
import app.toddlerenglish.ui.screens.StickerBookScreen
import app.toddlerenglish.ui.theme.Palette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val SCENE = "scene/{id}"
    const val GAMES = "games"
    const val GAME = "game/{id}"
    const val SONGS = "songs"
    const val STICKERS = "stickers"
    const val REST = "rest"
    const val GATE = "gate"
    const val PARENT = "parent"

    /** Routes where play time does not count. */
    val notPlaying = setOf(GATE, PARENT, REST)

    fun scene(id: String) = "scene/$id"

    fun game(id: String) = "game/$id"
}

@Composable
fun AppNavHost(container: AppContainer, nav: NavHostController = rememberNavController()) {
    val settings by container.settings.settings.collectAsState(initial = Settings())
    val progress by container.progress.progress.collectAsState(initial = Progress())
    var celebrating by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var celebrationCount by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val activity = LocalActivity.current
    val context = LocalContext.current
    val mascot = rememberMascotState()
    val content = container.content
    val audio = container.audio
    val songPlayer = remember { SongPlayer(context.applicationContext) }
    val timer = remember { SessionTimer() }
    val backStack by nav.currentBackStackEntryAsState()
    val route by rememberUpdatedState(backStack?.destination?.route)
    val limit by rememberUpdatedState(settings.sessionLimitMinutes)

    LaunchedEffect(settings.soundOn) {
        audio.enabled = settings.soundOn
        if (!settings.soundOn) audio.stop()
    }

    // Play-time limit: counts only while the app is on screen and the child is playing.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            var last = System.currentTimeMillis()
            while (true) {
                delay(1_000)
                val now = System.currentTimeMillis()
                val playing = route != null && route !in Routes.notPlaying
                val minutes = timer.tick(now - last, playing)
                last = now
                repeat(minutes) { container.progress.addMinute() }
                if (playing && timer.isRestTime(limit)) {
                    songPlayer.release()
                    nav.navigate(Routes.REST) { popUpTo(Routes.HOME) }
                }
            }
        }
    }

    // A new sticker: let the current word finish, then fly the sticker in and say so.
    LaunchedEffect(Unit) {
        container.rewarder.newStickers.collect { sticker ->
            delay(900)
            celebrating = celebrationCount++ to content.word(sticker.word)?.image.orEmpty()
            content.phrase("new_sticker")?.let { audio.play(it.audio) }
            mascot.celebrate()
            delay(2_200)
        }
    }

    fun tile(id: String, title: String, icon: String, phrase: String, tint: Color) = HomeTile(
        id = id,
        title = title,
        icon = content.word(icon)?.image.orEmpty(),
        tint = tint,
        phraseAudio = content.phrase(phrase)?.audio,
    )
    val homeTiles = listOf(
        tile(Routes.GAMES, "Games", "kite", "games", Palette.Sky.copy(alpha = 0.6f)),
        tile(Routes.SONGS, "Songs", "bird", "songs", Palette.Lilac.copy(alpha = 0.6f)),
        tile(Routes.STICKERS, "Stickers", "star", "stickers", Palette.Sun.copy(alpha = 0.45f)),
    )

    fun goHome() {
        audio.stop()
        if (!nav.popBackStack(Routes.HOME, inclusive = false)) nav.navigate(Routes.HOME)
    }

    fun gameEnv() = GameEnv(
        content = content,
        audio = audio,
        mascot = mascot,
        tier = settings.tier,
        onRoundComplete = { container.rewarder.gameRound(it) },
        onHome = ::goHome,
    )

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
                    onTile = { nav.navigate(it.id) },
                    onParent = { nav.navigate(Routes.GATE) },
                )
            }
            composable(Routes.SCENE) { entry ->
                val scene = content.scenes.firstOrNull { it.id == entry.arguments?.getString("id") }
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
            composable(Routes.GAMES) {
                GamesMenuScreen(gameEnv()) { game ->
                    nav.navigate(if (game.type == "singalong") Routes.SONGS else Routes.game(game.id))
                }
            }
            composable(Routes.GAME) { entry ->
                val game = content.games.firstOrNull { it.id == entry.arguments?.getString("id") }
                when (game?.type) {
                    "bubbles" -> BubblesScreen(gameEnv(), game)
                    "match" -> MatchScreen(gameEnv(), game)
                    "feed" -> FeedScreen(gameEnv(), game)
                    else -> {}
                }
            }
            composable(Routes.SONGS) {
                val game = content.games.firstOrNull { it.type == "singalong" }
                if (game != null) {
                    SingAlongScreen(gameEnv(), game, songPlayer) { song -> container.rewarder.songFinished(song.id) }
                }
            }
            composable(Routes.STICKERS) {
                StickerBookScreen(content = content, progress = progress, audio = audio, mascot = mascot, onHome = ::goHome)
            }
            composable(Routes.REST) {
                RestScreen(content = content, audio = audio, mascot = mascot, onParent = { nav.navigate(Routes.GATE) })
            }
            composable(Routes.GATE) {
                ParentalGateScreen(
                    onSpoken = { content.phrase("grown_up")?.let { audio.play(it.audio) } },
                    onOpen = {
                        timer.reset()
                        mascot.idle()
                        nav.navigate(Routes.PARENT) { popUpTo(Routes.HOME) }
                    },
                    // From the rest screen, leaving the gate goes back to resting, not to play.
                    onCancel = { if (!nav.popBackStack()) goHome() },
                )
            }
            composable(Routes.PARENT) {
                ParentCornerScreen(
                    settings = settings,
                    progress = progress,
                    content = content,
                    actions = ParentActions(
                        onSoundOn = { scope.launch { container.settings.setSoundOn(it) } },
                        onTier = { scope.launch { container.settings.setTier(it) } },
                        onSessionLimit = { scope.launch { container.settings.setSessionLimit(it) } },
                        onResetProgress = { scope.launch { container.progress.reset() } },
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
