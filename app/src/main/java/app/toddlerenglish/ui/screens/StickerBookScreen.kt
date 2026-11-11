package app.toddlerenglish.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.toddlerenglish.audio.WordAudio
import app.toddlerenglish.content.Content
import app.toddlerenglish.data.Progress
import app.toddlerenglish.ui.components.Mascot
import app.toddlerenglish.ui.components.MascotState
import app.toddlerenglish.ui.components.StickerBadge
import app.toddlerenglish.ui.components.StickerSpot
import app.toddlerenglish.ui.components.TapSparkles
import app.toddlerenglish.ui.components.bouncyClickable
import app.toddlerenglish.ui.theme.Dimens
import app.toddlerenglish.ui.theme.Palette

/**
 * The sticker book. Earned stickers come first, then soft empty spots for the ones still to find.
 * Tapping a sticker says its word; tapping an empty spot gets an invitation to play.
 */
@Composable
fun StickerBookScreen(content: Content, progress: Progress, audio: WordAudio, mascot: MascotState, onHome: () -> Unit) {
    BackHandler(onBack = onHome)
    LaunchedEffect(Unit) { content.phrase("stickers")?.let { audio.play(it.audio) } }
    val owned = progress.stickers.mapNotNull { id -> content.stickers.firstOrNull { it.id == id } }
    val empty = content.stickers.size - owned.size
    TapSparkles(Modifier.fillMaxSize().background(Palette.Cream), onEmptyTap = { mascot.encourage() }) {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(110.dp),
                contentPadding = PaddingValues(start = Dimens.SpaceM, end = Dimens.SpaceM, top = 88.dp, bottom = 110.dp),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
            ) {
                items(owned, key = { it.id }) { sticker ->
                    val word = content.word(sticker.word)
                    Box(Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                        StickerBadge(
                            word?.image ?: "",
                            100.dp,
                            Modifier
                                .bouncyClickable(word?.text ?: "sticker") {
                                    word?.let { audio.play(it.audio) }
                                    mascot.celebrate()
                                }
                                .semantics { contentDescription = "${word?.text} sticker" },
                        )
                    }
                }
                items(empty) {
                    Box(Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                        StickerSpot(
                            84.dp,
                            Modifier.bouncyClickable("Empty sticker spot") {
                                content.phrase("games")?.let { audio.play(it.audio) }
                                mascot.encourage()
                            },
                        )
                    }
                }
            }
            HomeButton(onHome, Modifier.align(Alignment.TopStart).padding(Dimens.SpaceS))
            Mascot(mascot, Modifier.align(Alignment.BottomStart).padding(Dimens.SpaceS).size(84.dp))
        }
    }
}
