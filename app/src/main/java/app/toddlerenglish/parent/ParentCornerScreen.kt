package app.toddlerenglish.parent

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import app.toddlerenglish.BuildConfig
import app.toddlerenglish.data.Settings
import app.toddlerenglish.ui.theme.Dimens
import app.toddlerenglish.ui.theme.Palette

/** Callbacks the parent corner needs. Sections are filled in by later sessions. */
data class ParentActions(
    val onSoundOn: (Boolean) -> Unit,
    val onTier: (Int) -> Unit,
    val onSessionLimit: (Int) -> Unit,
    val onResetProgress: () -> Unit,
    val onBack: () -> Unit,
    val onCloseApp: () -> Unit,
)

@Composable
fun ParentCornerScreen(settings: Settings, actions: ParentActions, progress: @Composable () -> Unit = {}) {
    BackHandler(onBack = actions.onBack)
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
        progress()
        Section("Sound") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = Dimens.TouchTarget)
                    .semantics { role = Role.Switch },
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
            OutlinedButton(onClick = actions.onCloseApp, modifier = Modifier.heightIn(min = Dimens.TouchTarget)) {
                Text("Close the app")
            }
        }
        Button(onClick = actions.onBack, modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.TouchTarget)) {
            Text("Back to playing", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.heightIn(min = Dimens.SpaceS))
        Text(
            "Toddler English ${BuildConfig.VERSION_NAME}. Works offline. Collects no data.",
            style = MaterialTheme.typography.bodyMedium,
            color = Palette.InkSoft,
        )
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
