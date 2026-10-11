package com.dinoexpert.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dinoexpert.R
import com.dinoexpert.ui.theme.Ink
import kotlin.math.sin

/** Rendering is replaceable; game logic communicates only an expression state. */
enum class ProfessorState { Idle, Asking, Thinking, Celebrating, Congratulations }

@Composable
fun ProfessorFossilCharacter(state: ProfessorState, reducedAnimation: Boolean, modifier: Modifier = Modifier) {
    val description = stringResource(when (state) {
        ProfessorState.Idle -> R.string.character_idle
        ProfessorState.Asking -> R.string.character_asking
        ProfessorState.Thinking -> R.string.character_thinking
        ProfessorState.Celebrating -> R.string.character_celebrating
        ProfessorState.Congratulations -> R.string.character_congratulations
    })
    val phase = if (reducedAnimation) 0f else characterPhase()
    val happy = state == ProfessorState.Celebrating || state == ProfessorState.Congratulations
    val successLift = remember { Animatable(0f) }
    LaunchedEffect(state, reducedAnimation) {
        successLift.snapTo(0f)
        if (happy && !reducedAnimation) {
            successLift.animateTo(1f, tween(180))
            successLift.animateTo(0f, tween(240))
        }
    }
    val blink = !reducedAnimation && phase > .96f
    Canvas(modifier.width(270.dp).height(230.dp).semantics { contentDescription = description }) {
        val scale = minOf(size.width / 280f, size.height / 240f)
        withTransform({
            translate((size.width - 280 * scale) / 2, (size.height - 240 * scale) / 2)
            scale(scale, scale, Offset.Zero)
        }) {
            drawOval(Color(0xFFE5DEBD), Offset(44f, 216f), Size(198f, 15f))
            withTransform({ translate(0f, if (reducedAnimation) 0f else sin(phase * 6.283f) * 2f - successLift.value * 8f) }) {
                // Tail, explorer coat and feet.
                val tail = Path().apply { moveTo(196f, 182f); quadraticTo(267f, 191f, 263f, 153f); quadraticTo(245f, 197f, 194f, 154f); close() }
                drawPath(tail, Color(0xFF54A87A))
                drawRoundRect(Color(0xFF3D8866), Offset(80f, 192f), Size(48f, 33f), CornerRadius(14f))
                drawRoundRect(Color(0xFF3D8866), Offset(151f, 192f), Size(48f, 33f), CornerRadius(14f))
                drawRoundRect(Color(0xFFE9C16C), Offset(77f, 123f), Size(126f, 82f), CornerRadius(36f))
                val collar = Path().apply { moveTo(104f, 127f); lineTo(140f, 154f); lineTo(176f, 127f); close() }
                drawPath(collar, Color(0xFFFFE8AA))
                drawLine(Ink.copy(alpha = .4f), Offset(140f, 155f), Offset(140f, 198f), 2f)
                drawCircle(Ink, 2.5f, Offset(146f, 174f))
                drawCircle(Ink, 2.5f, Offset(146f, 188f))
                // Arms change pose for success.
                drawLine(Color(0xFF54A87A), Offset(194f, 149f), Offset(225f, if (happy) 94f else 169f), 22f)
                drawCircle(Color(0xFF54A87A), 13f, Offset(225f, if (happy) 94f else 169f))
                drawLine(Color(0xFF54A87A), Offset(88f, 150f), Offset(70f, 180f), 22f)
                // Notebook held against the jacket.
                drawRoundRect(Color(0xFF006B70), Offset(59f, 167f), Size(49f, 45f), CornerRadius(6f))
                drawRoundRect(Color(0xFFFFFCF4), Offset(66f, 174f), Size(34f, 31f), CornerRadius(2f))
                repeat(3) { i -> drawLine(Color(0xFF799A8D), Offset(71f, 181f + i * 7f), Offset(94f, 181f + i * 7f), 2f) }
                // Head, snout and ears/spines.
                listOf(Offset(101f, 30f), Offset(135f, 19f), Offset(170f, 29f)).forEach {
                    drawCircle(Color(0xFF2B7253), 12f, it)
                }
                drawRoundRect(Color(0xFF54A87A), Offset(65f, 29f), Size(145f, 116f), CornerRadius(52f))
                drawOval(Color(0xFF8FCBA1), Offset(81f, 84f), Size(117f, 61f))
                // Eyes and round glasses.
                listOf(111f, 165f).forEach { x ->
                    drawOval(Color(0xFFFFFEF8), Offset(x - 19f, 47f), Size(38f, 42f))
                    if (blink) drawLine(Ink, Offset(x - 9f, 70f), Offset(x + 9f, 70f), 3f)
                    else {
                        drawOval(Ink, Offset(x - 6f, if (state == ProfessorState.Thinking) 62f else 59f), Size(13f, 20f))
                        drawCircle(Color.White, 3f, Offset(x + 2f, 64f))
                    }
                    drawCircle(Ink, 25f, Offset(x, 68f), style = Stroke(3.5f))
                }
                drawLine(Ink, Offset(136f, 67f), Offset(140f, 67f), 3.5f)
                drawLine(Ink, Offset(65f, 62f), Offset(85f, 66f), 3f)
                drawLine(Ink, Offset(190f, 66f), Offset(210f, 62f), 3f)
                drawCircle(Color(0xFF2B7253), 3f, Offset(117f, 100f))
                drawCircle(Color(0xFF2B7253), 3f, Offset(163f, 100f))
                val smile = Path().apply {
                    moveTo(110f, 115f)
                    quadraticTo(140f, if (happy) 151f else if (state == ProfessorState.Thinking) 124f else 139f, 172f, 115f)
                }
                drawPath(smile, Ink, style = Stroke(if (happy) 4f else 3f))
                // Explorer hat.
                drawRoundRect(Color(0xFFE8BB65), Offset(76f, 23f), Size(125f, 17f), CornerRadius(8f))
                drawRoundRect(Color(0xFFF4CF82), Offset(104f, 5f), Size(71f, 25f), CornerRadius(12f))
                drawRoundRect(Color(0xFF986E36), Offset(104f, 22f), Size(71f, 6f), CornerRadius(2f))
                if (happy) {
                    listOf(Offset(37f, 75f), Offset(245f, 45f), Offset(230f, 130f)).forEach { center ->
                        drawLine(Color(0xFFC17825), center - Offset(6f, 0f), center + Offset(6f, 0f), 3f)
                        drawLine(Color(0xFFC17825), center - Offset(0f, 6f), center + Offset(0f, 6f), 3f)
                    }
                }
            }
        }
    }
}

@Composable
private fun characterPhase(): Float {
    val transition = rememberInfiniteTransition(label = "Professor breathing and blinking")
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "gentle movement",
    )
    return phase
}
