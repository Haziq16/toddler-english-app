package app.toddlerenglish.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.toddlerenglish.R
import app.toddlerenglish.ui.theme.Dimens
import app.toddlerenglish.ui.theme.Palette

/** The padlock that leads to the parental gate. Quiet looking, but still a full 64dp target. */
@Composable
fun ParentButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(Dimens.TouchTarget)
            .background(Palette.Paper.copy(alpha = 0.7f), CircleShape)
            .bouncyClickable("Grown-ups", onClick = onClick)
            .testTag("parent_button"),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(R.drawable.ic_parent), contentDescription = "Grown-ups", modifier = Modifier.size(28.dp))
    }
}
