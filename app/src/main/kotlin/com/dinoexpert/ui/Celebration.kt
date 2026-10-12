package com.dinoexpert.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dinoexpert.R
import com.dinoexpert.ui.theme.Forest
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun FossilBadge(level: Int, earned: Boolean, modifier: Modifier = Modifier) {
    val label = stringResource(if (earned) R.string.earned_badge else R.string.unearned_badge, levelTitle(level))
    Canvas(modifier.size(76.dp).semantics { contentDescription = label }) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension * .43f
        val accent = if (earned) Color(0xFFAB6B23) else Color(0xFF648071)
        drawCircle(if (earned) Color(0xFFFFE4AE) else Color(0xFFE1EAE0), radius, center)
        drawCircle(accent, radius, center, style = Stroke(size.minDimension * .035f))
        repeat(12) { index ->
            val angle = index * Math.PI / 6
            drawCircle(accent.copy(alpha = .55f), size.minDimension * .015f,
                center + Offset(cos(angle).toFloat(), sin(angle).toFloat()) * (radius * .85f))
        }
        val unit = radius / 3f
        when (level) {
            1 -> {
                drawLine(accent, center - Offset(unit, -unit), center + Offset(unit, -unit), unit * .8f)
                listOf(Offset(-unit, unit), Offset(unit, -unit)).forEach { end ->
                    drawCircle(accent, unit * .55f, center + end + Offset(0f, unit * .3f))
                    drawCircle(accent, unit * .55f, center + end - Offset(0f, unit * .3f))
                }
            }
            2 -> {
                val leaf = Path().apply {
                    moveTo(center.x - unit, center.y + unit)
                    quadraticTo(center.x - unit * 2, center.y - unit * 2, center.x + unit * 1.7f, center.y - unit * 1.7f)
                    quadraticTo(center.x + unit * 2, center.y + unit * 2, center.x - unit, center.y + unit)
                }
                drawPath(leaf, accent)
                drawLine(Color(0xFFFFF6DA), center - Offset(unit * .6f, -unit * .6f), center + Offset(unit, -unit), unit * .15f)
            }
            3 -> {
                val hourglass = Path().apply {
                    moveTo(center.x - unit, center.y - unit * 1.5f); lineTo(center.x + unit, center.y - unit * 1.5f)
                    lineTo(center.x - unit, center.y + unit * 1.5f); lineTo(center.x + unit, center.y + unit * 1.5f); close()
                }
                drawPath(hourglass, accent, style = Stroke(unit * .25f))
            }
            4 -> {
                drawCircle(accent, unit, center - Offset(unit * .4f, unit * .4f), style = Stroke(unit * .3f))
                drawLine(accent, center + Offset(unit * .4f, unit * .4f), center + Offset(unit * 1.5f, unit * 1.5f), unit * .4f)
            }
            else -> {
                val star = Path()
                repeat(10) { i ->
                    val angle = i * Math.PI / 5 - Math.PI / 2
                    val r = unit * if (i % 2 == 0) 1.8f else .8f
                    val point = center + Offset(cos(angle).toFloat(), sin(angle).toFloat()) * r
                    if (i == 0) star.moveTo(point.x, point.y) else star.lineTo(point.x, point.y)
                }
                star.close(); drawPath(star, accent)
            }
        }
    }
}

@Composable
fun CelebrationConfetti(reducedAnimation: Boolean, modifier: Modifier = Modifier) {
    if (reducedAnimation) return
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(1800, easing = LinearEasing)) }
    val particles = remember {
        val random = Random(731)
        List(32) { Triple(random.nextFloat(), random.nextFloat(), random.nextInt(3)) }
    }
    Canvas(modifier) {
        val t = progress.value
        if (t >= 1f) return@Canvas
        val colors = listOf(Forest, Color(0xFFE9B351), Color(0xFF247F8D))
        particles.forEach { (x, speed, color) ->
            val point = Offset(x * size.width + sin(t * 8 + speed) * 12,
                (t * (1.1f + speed * .4f) - .1f) * size.height)
            drawRoundRect(colors[color].copy(alpha = 1 - t), point, Size(7.dp.toPx(), 11.dp.toPx()))
        }
    }
}
