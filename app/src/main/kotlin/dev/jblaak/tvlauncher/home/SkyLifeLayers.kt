package dev.jblaak.tvlauncher.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.tooling.preview.Preview
import dev.jblaak.tvlauncher.sky.SkyLife
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

internal fun DrawScope.drawSkyLife(life: SkyLife, seconds: Float) {
    if (life.shootingStars) {
        drawMeteor(Offset(0.22f * 1280, 0.09f * 720), Size(170f, 2f), 25f, Color.White.copy(alpha = 0.95f), progress(seconds, 13f, 4f))
        drawMeteor(Offset(0.58f * 1280, 0.04f * 720), Size(120f, 1.5f), 32f, Color(220, 235, 255).copy(alpha = 0.9f), progress(seconds, 21f, 15f))
    }
    if (life.geese) drawGeese(seconds)
}

private fun DrawScope.drawMeteor(topLeft: Offset, size: Size, degrees: Float, head: Color, progress: Float) {
    if (progress < 0.86f || progress >= 0.93f) return
    val alpha = if (progress < 0.87f) (progress - 0.86f) / 0.01f else (0.93f - progress) / 0.06f
    val travelled = (progress - 0.86f) / 0.07f
    val start = topLeft + Offset(340f, 160f) * travelled
    val centre = start + Offset(size.width / 2, size.height / 2)
    rotate(degrees, pivot = centre) {
        drawRect(
            Brush.horizontalGradient(0f to head.copy(alpha = 0f), 1f to head, startX = start.x, endX = start.x + size.width),
            topLeft = start,
            size = size,
            alpha = alpha,
        )
    }
}

private fun DrawScope.drawGeese(seconds: Float) {
    val flight = (progress(seconds, 34f, 6f) / 0.55f).coerceAtMost(1f)
    val offset = Offset(-1800f, -150f) * flight
    for (bird in 0 until 9) {
        val row = ceil(bird / 2f)
        val side = when {
            bird == 0 -> 0
            bird % 2 == 1 -> -1
            else -> 1
        }
        val position = Offset(1320f + 22f * row, 170f + 13f * side * row) + offset
        val flap = flap(seconds + seeded(bird + 730) * 0.34f)
        drawWing(position, flap)
        drawWing(position + Offset(9f, 0f), flap)
    }
}

private fun DrawScope.drawWing(topLeft: Offset, flap: Float) {
    scale(scaleX = 1f, scaleY = flap, pivot = topLeft + Offset(4.5f, 3.5f)) {
        drawArc(
            color = Color(14, 14, 24).copy(alpha = 0.78f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = topLeft + Offset(0f, 1f),
            size = Size(9f, 5f),
            style = Stroke(width = 2f, cap = StrokeCap.Butt),
        )
    }
}

private fun flap(seconds: Float): Float {
    val cycle = seconds / 0.34f
    val leg = cycle - floor(cycle)
    val forward = floor(cycle).toInt() % 2 == 0
    val eased = (1 - cos(PI * (if (forward) leg else 1 - leg))).toFloat() / 2
    return 1f - 1.5f * eased
}

internal fun progress(seconds: Float, period: Float, lead: Float): Float {
    val cycle = (seconds + lead) / period
    return cycle - floor(cycle)
}

private fun seeded(index: Int): Float {
    val value = sin(index * 12.9898 + 78.233) * 43758.5453
    return (value - floor(value)).toFloat()
}

@Preview(device = "id:tv_1080p")
@Composable
internal fun SkyLifePreview() {
    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFF1A1E40))) {
        scale(size.width / 1280f, pivot = Offset.Zero) {
            drawSkyLife(SkyLife(shootingStars = true, geese = true), seconds = 3.8f)
        }
    }
}
