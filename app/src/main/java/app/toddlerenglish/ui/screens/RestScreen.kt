package app.toddlerenglish.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.toddlerenglish.audio.WordAudio
import app.toddlerenglish.content.Content
import app.toddlerenglish.ui.components.ContentImage
import app.toddlerenglish.ui.components.Mascot
import app.toddlerenglish.ui.components.MascotState
import app.toddlerenglish.ui.components.ParentButton
import app.toddlerenglish.ui.components.TapSparkles
import app.toddlerenglish.ui.components.bouncyClickable
import app.toddlerenglish.ui.theme.Dimens
import app.toddlerenglish.ui.theme.Palette

/**
 * Shown when the parent's play-time limit is reached. Calm: Pebble sleeps under the moon, nothing ticks down,
 * no alarm. Taps still get a soft sparkle. Only a grown-up (through the gate) can go back to playing.
 */
@Composable
fun RestScreen(content: Content, audio: WordAudio, mascot: MascotState, onParent: () -> Unit) {
    BackHandler(enabled = true) { }
    LaunchedEffect(Unit) {
        audio.stop()
        mascot.sleep()
        content.phrase("rest")?.let { audio.play(it.audio) }
    }
    TapSparkles(Modifier.fillMaxSize().background(RestTint).testTag("rest"), onEmptyTap = { }) {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            ContentImage(
                content.word("moon")?.image.orEmpty(),
                null,
                Modifier.align(Alignment.TopCenter).padding(top = 64.dp).size(140.dp),
            )
            Mascot(
                mascot,
                Modifier
                    .align(Alignment.Center)
                    .size(220.dp)
                    .bouncyClickable("Pebble is resting") { content.phrase("rest")?.let { audio.play(it.audio) } },
            )
            ParentButton(onParent, Modifier.align(Alignment.TopEnd).padding(Dimens.SpaceS))
        }
    }
}

/** Night-time tint for the rest screen. */
val RestTint = Palette.sceneTint("bedtime")
