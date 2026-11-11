package app.toddlerenglish.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.toddlerenglish.audio.WordAudio
import app.toddlerenglish.content.Content
import app.toddlerenglish.content.SceneInfo
import app.toddlerenglish.ui.components.ContentImage
import app.toddlerenglish.ui.components.bouncyClickable
import app.toddlerenglish.ui.theme.Dimens
import app.toddlerenglish.ui.theme.Palette

/** S2 stand-in: the scene's words as a grid. S3 replaces it with the data-driven scene renderer. */
@Composable
fun SceneScreen(scene: SceneInfo, content: Content, audio: WordAudio, onHome: () -> Unit) {
    BackHandler(onBack = onHome)
    val words = content.wordsInScene(scene.id)
    LazyVerticalGrid(
        columns = GridCells.Adaptive(110.dp),
        contentPadding = PaddingValues(Dimens.SpaceM),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        modifier = Modifier.fillMaxSize().background(Palette.sceneTint(scene.tint)).safeDrawingPadding(),
    ) {
        items(words, key = { it.id }) { word ->
            ContentImage(
                word.image,
                contentDescription = word.text,
                modifier = Modifier.aspectRatio(1f).bouncyClickable(word.text) { audio.play(word.audio) },
            )
        }
    }
}
