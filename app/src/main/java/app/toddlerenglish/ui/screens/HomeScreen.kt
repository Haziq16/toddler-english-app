package app.toddlerenglish.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.toddlerenglish.audio.WordAudio
import app.toddlerenglish.content.Content
import app.toddlerenglish.content.SceneInfo
import app.toddlerenglish.ui.components.ContentImage
import app.toddlerenglish.ui.components.Mascot
import app.toddlerenglish.ui.components.MascotState
import app.toddlerenglish.ui.components.ParentButton
import app.toddlerenglish.ui.components.TapSparkles
import app.toddlerenglish.ui.components.bouncyClickable
import app.toddlerenglish.ui.theme.Dimens
import app.toddlerenglish.ui.theme.Palette

/** A big tile on the home screen: a scene, or an activity such as games or songs. */
data class HomeTile(val id: String, val title: String, val icon: String, val tint: Color, val phraseAudio: String?)

@Composable
fun HomeScreen(
    content: Content,
    audio: WordAudio,
    mascot: MascotState,
    greet: Boolean,
    extraTiles: List<HomeTile>,
    onGreeted: () -> Unit,
    onScene: (SceneInfo) -> Unit,
    onTile: (HomeTile) -> Unit,
    onParent: () -> Unit,
) {
    // A toddler pressing Back on the home screen must not leave the app. Grown-ups exit from the parent corner.
    BackHandler(enabled = true) { mascot.encourage() }

    LaunchedEffect(greet) {
        if (greet) {
            content.phrase("hello")?.let { audio.play(it.audio) }
            mascot.celebrate()
            onGreeted()
        }
    }

    TapSparkles(Modifier.fillMaxSize().background(Palette.Cream), onEmptyTap = { mascot.encourage() }) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Mascot(
                    state = mascot,
                    modifier = Modifier
                        .size(96.dp)
                        .bouncyClickable("Pebble says hello") {
                            content.phrase("hello")?.let { audio.play(it.audio) }
                            mascot.celebrate()
                        },
                )
                Spacer(Modifier.weight(1f))
                ParentButton(onClick = onParent)
            }
            val scenes = content.scenes
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(Dimens.SpaceM),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(scenes, key = { it.id }) { scene ->
                    Tile(
                        title = scene.title,
                        icon = content.word(scene.icon)?.image ?: "",
                        tint = Palette.sceneTint(scene.tint),
                    ) {
                        content.phrase(scene.phrase)?.let { audio.play(it.audio) }
                        onScene(scene)
                    }
                }
                if (extraTiles.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.size(Dimens.SpaceS)) }
                }
                items(extraTiles, key = { it.id }) { tile ->
                    Tile(title = tile.title, icon = tile.icon, tint = tile.tint) {
                        tile.phraseAudio?.let(audio::play)
                        onTile(tile)
                    }
                }
            }
        }
    }
}

@Composable
private fun Tile(title: String, icon: String, tint: Color, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(tint, RoundedCornerShape(Dimens.Corner))
            .bouncyClickable(title, onClick = onClick)
            .semantics { contentDescription = title }
            .padding(Dimens.SpaceM),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.25f), contentAlignment = Alignment.Center) {
            ContentImage(icon, contentDescription = null, modifier = Modifier.fillMaxSize(0.9f))
        }
        Text(title, style = MaterialTheme.typography.titleLarge, color = Palette.Ink)
    }
}
