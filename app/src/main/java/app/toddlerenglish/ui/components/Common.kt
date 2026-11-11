package app.toddlerenglish.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import app.toddlerenglish.ui.theme.Dimens
import app.toddlerenglish.ui.theme.Palette

/**
 * Child-friendly click: at least 64dp, shrinks a little while pressed, no ripple.
 * Every tappable thing in the child area uses this.
 */
fun Modifier.bouncyClickable(label: String, enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, label = "press")
    this
        .defaultMinSize(Dimens.TouchTarget, Dimens.TouchTarget)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            onClickLabel = label,
            role = Role.Button,
            onClick = onClick,
        )
        // One clear spoken label for TalkBack; child text (tile titles) is not read twice.
        .clearAndSetSemantics { contentDescription = label }
}

/** Resolves a drawable by its content name, so art is swappable by file name (D-014). */
@SuppressLint("DiscouragedApi")
@Composable
fun rememberDrawableId(name: String): Int {
    val context = LocalContext.current
    val resources = LocalResources.current
    return remember(name) { resources.getIdentifier(name, "drawable", context.packageName) }
}

@Composable
fun ContentImage(name: String, contentDescription: String?, modifier: Modifier = Modifier) {
    val id = rememberDrawableId(name)
    if (id != 0) {
        Image(painterResource(id), contentDescription, modifier)
    } else {
        Box(modifier.background(Palette.Lilac, CircleShape))
    }
}
