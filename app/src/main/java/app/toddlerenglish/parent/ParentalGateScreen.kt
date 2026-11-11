package app.toddlerenglish.parent

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.toddlerenglish.ui.theme.Dimens
import app.toddlerenglish.ui.theme.Palette

/**
 * The parental gate. A child sees a calm screen; Pebble's voice says it is for grown-ups.
 * Wrong answers just start again: no sound, no message aimed at the child.
 */
@Composable
fun ParentalGateScreen(onSpoken: () -> Unit, onOpen: () -> Unit, onCancel: () -> Unit) {
    val gate = remember { ParentalGate() }
    var step by remember { mutableStateOf(gate.step) }
    var holding by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    BackHandler(onBack = onCancel)
    LaunchedEffect(Unit) { onSpoken() }
    LaunchedEffect(step) { if (step == ParentalGate.Step.Open) onOpen() }
    LaunchedEffect(holding) {
        while (holding) {
            withFrameMillis { }
            val now = SystemClock.uptimeMillis()
            progress = gate.holdProgress(now)
            gate.tick(now)
            step = gate.step
            if (step != ParentalGate.Step.Hold) holding = false
        }
        progress = 0f
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Palette.Mist)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.SpaceL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
    ) {
        Text("For grown-ups", style = MaterialTheme.typography.headlineLarge, color = Palette.Ink)
        when (val s = step) {
            ParentalGate.Step.Hold, ParentalGate.Step.Open -> {
                Text(
                    "Press and hold the circle for 3 seconds.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Palette.InkSoft,
                    textAlign = TextAlign.Center,
                )
                Box(
                    Modifier
                        .size(160.dp)
                        .testTag("gate_hold")
                        .semantics { contentDescription = "Hold for 3 seconds" }
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                gate.holdStart(SystemClock.uptimeMillis())
                                holding = true
                                tryAwaitRelease()
                                gate.holdEnd(SystemClock.uptimeMillis())
                                holding = false
                                step = gate.step
                            })
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(Modifier.size(160.dp)) {
                        drawCircle(Palette.Paper)
                        drawArc(
                            Palette.PebbleBody,
                            -90f,
                            360f * progress,
                            false,
                            style = Stroke(width = 12.dp.toPx()),
                        )
                    }
                    Text("Hold", style = MaterialTheme.typography.titleLarge, color = Palette.Ink)
                }
            }
            is ParentalGate.Step.Question -> {
                Text(
                    "What is ${s.text}?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Palette.Ink,
                    textAlign = TextAlign.Center,
                )
                Text(
                    s.entered.padEnd(s.answer.toString().length, '_').toList().joinToString(" "),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Palette.Ink,
                )
                Keypad { d ->
                    gate.digit(d)
                    step = gate.step
                }
            }
        }
        Spacer(Modifier.height(Dimens.SpaceS))
        TextButton(
            onClick = onCancel,
            colors = ButtonDefaults.textButtonColors(contentColor = Palette.Ink),
            modifier = Modifier.height(Dimens.TouchTarget),
        ) {
            Text("Back to playing", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun Keypad(onDigit: (Int) -> Unit) {
    val rows = listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9), listOf(0))
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS), horizontalAlignment = Alignment.CenterHorizontally) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS)) {
                row.forEach { d ->
                    Box(
                        Modifier
                            .size(Dimens.TouchTarget + 8.dp)
                            .background(Palette.Paper, RoundedCornerShape(Dimens.CornerSmall))
                            .border(1.dp, Palette.InkSoft.copy(alpha = 0.2f), RoundedCornerShape(Dimens.CornerSmall))
                            .testTag("gate_key_$d")
                            .pointerInput(d) { detectTapGestures { onDigit(d) } }
                            .semantics { contentDescription = "$d" },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("$d", style = MaterialTheme.typography.headlineMedium, color = Palette.Ink)
                    }
                }
            }
        }
    }
}
