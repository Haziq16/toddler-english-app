package app.toddlerenglish.parent

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.toddlerenglish.BuildConfig
import app.toddlerenglish.content.Content
import app.toddlerenglish.data.Progress
import app.toddlerenglish.data.Settings
import app.toddlerenglish.ui.theme.Dimens
import app.toddlerenglish.ui.theme.Palette

data class ParentActions(
    val onSoundOn: (Boolean) -> Unit,
    val onTier: (Int) -> Unit,
    val onSessionLimit: (Int) -> Unit,
    val onResetProgress: () -> Unit,
    val onBack: () -> Unit,
    val onCloseApp: () -> Unit,
)

private val tierInfo = listOf(
    1 to ("First words" to "Short everyday words, 2 pictures to choose from, slow bubbles. Ages 1-2."),
    2 to ("More words" to "Adds more animals, food and things, 3 pictures, Pebble asks for a food. Ages 2-4."),
    3 to ("All words" to "Every word, 4 pictures, faster bubbles, song lyrics shown. Ages 4-5."),
)

@Composable
fun ParentCornerScreen(settings: Settings, progress: Progress, content: Content, actions: ParentActions) {
    BackHandler(onBack = actions.onBack)
    var confirmReset by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .background(Palette.Mist)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.SpaceL),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
    ) {
        Text("Parent corner", style = MaterialTheme.typography.headlineLarge, color = Palette.Ink)
        Button(
            onClick = actions.onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Palette.PebbleBody, contentColor = Palette.Ink),
            modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.TouchTarget),
        ) { Text("Back to playing", style = MaterialTheme.typography.labelLarge) }

        ProgressSection(progress, content)

        Section("Word level") {
            tierInfo.forEach { (tier, info) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = Dimens.TouchTarget)
                        .selectable(selected = settings.tier == tier, role = Role.RadioButton) { actions.onTier(tier) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = settings.tier == tier, onClick = null)
                    Column(Modifier.padding(start = Dimens.SpaceS)) {
                        Text(info.first, style = MaterialTheme.typography.bodyLarge, color = Palette.Ink)
                        Text(info.second, style = MaterialTheme.typography.bodyMedium, color = Palette.InkSoft)
                    }
                }
            }
        }

        Section("Play time") {
            Text(
                "After this much play, Pebble goes to sleep and play stops until a grown-up opens the gate. " +
                    "Your child never sees a countdown.",
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.InkSoft,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS)) {
                Settings.SESSION_LIMITS.forEach { m ->
                    FilterChip(
                        selected = settings.sessionLimitMinutes == m,
                        onClick = { actions.onSessionLimit(m) },
                        label = { Text(if (m == 0) "No limit" else "$m min") },
                        modifier = Modifier.heightIn(min = 48.dp),
                    )
                }
            }
        }

        Section("Sound") {
            Row(
                Modifier.fillMaxWidth().heightIn(min = Dimens.TouchTarget).semantics { role = Role.Switch },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Voice and sounds", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = settings.soundOn, onCheckedChange = actions.onSoundOn)
            }
        }

        Section("Leave the app") {
            Text(
                "Back does not leave the app, so a child cannot exit by accident. Use this button to close it.",
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.InkSoft,
            )
            OutlinedButton(onClick = actions.onCloseApp, modifier = Modifier.heightIn(min = Dimens.TouchTarget)) { Text("Close the app") }
        }

        Section("Start over") {
            Text(
                "Clears words heard, stickers and minutes played. Settings stay.",
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.InkSoft,
            )
            OutlinedButton(onClick = {
                confirmReset = true
            }, modifier = Modifier.heightIn(min = Dimens.TouchTarget)) { Text("Reset progress") }
        }

        Text(
            "Toddler English ${BuildConfig.VERSION_NAME}. Works offline. Collects no data: progress stays on this device.",
            style = MaterialTheme.typography.bodyMedium,
            color = Palette.InkSoft,
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset progress?") },
            text = { Text("Words heard, stickers and minutes played will be cleared. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    actions.onResetProgress()
                }) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Keep progress") } },
        )
    }
}

@Composable
private fun ProgressSection(progress: Progress, content: Content) {
    Section("Progress") {
        Text(
            "${progress.distinctWordsHeard} of ${content.words.size} words heard · " +
                "${progress.stickers.size} of ${content.stickers.size} stickers · ${progress.minutesPlayed} ${if (progress.minutesPlayed == 1) "minute" else "minutes"} played",
            style = MaterialTheme.typography.bodyLarge,
            color = Palette.Ink,
        )
        content.scenes.forEach { scene ->
            val words = content.wordsInScene(scene.id)
            val heard = words.count { (progress.heard[it.id] ?: 0) > 0 }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(scene.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(96.dp))
                Box(Modifier.weight(1f).height(14.dp).background(Palette.Mist, RoundedCornerShape(7.dp))) {
                    if (heard > 0) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(heard.toFloat() / words.size)
                                .background(Palette.PebbleBody, RoundedCornerShape(7.dp)),
                        )
                    }
                }
                Text("  $heard/${words.size}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        val favourites = progress.heard.entries.sortedByDescending { it.value }.take(5).mapNotNull { content.word(it.key)?.text }
        if (favourites.isNotEmpty()) {
            Text("Heard most: ${favourites.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium, color = Palette.InkSoft)
        }
        val rounds = progress.gameRounds.entries.joinToString(", ") { (id, n) ->
            "${content.games.firstOrNull { it.id == id }?.title ?: id} $n"
        }
        if (rounds.isNotEmpty()) Text("Game rounds: $rounds", style = MaterialTheme.typography.bodyMedium, color = Palette.InkSoft)
    }
}

@Composable
fun Section(title: String, body: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Palette.Paper, RoundedCornerShape(Dimens.CornerSmall))
            .padding(Dimens.SpaceM),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = Palette.Ink)
        body()
    }
}
