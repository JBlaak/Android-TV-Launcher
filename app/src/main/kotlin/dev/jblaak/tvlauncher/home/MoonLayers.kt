package dev.jblaak.tvlauncher.home

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import dev.jblaak.tvlauncher.sky.Moon
import dev.jblaak.tvlauncher.sky.MoonPhase
import kotlin.math.abs
import kotlin.math.sqrt

internal fun DrawScope.drawMoon(moon: Moon) {
    when (moon) {
        is Moon.Disc -> drawDisc(moon)

        is Moon.Glow -> {
            val glow = Color(240, 236, 215)
            val radius = 180f * sqrt(2f)
            drawCircle(
                Brush.radialGradient(0f to glow.copy(alpha = moon.opacity.toFloat()), 0.6f to glow.copy(alpha = 0f), center = moon.position.toOffset(), radius = radius),
                radius = 180f,
                center = moon.position.toOffset(),
            )
        }

        Moon.Hidden -> Unit
    }
}

private fun DrawScope.drawDisc(moon: Moon.Disc) {
    val centre = moon.position.toOffset()
    val opacity = moon.opacity.toFloat()
    val radius = 24f * 64f / 60f
    drawShadow(centre, radius, blur = 48f, alpha = 0.25f * opacity * moon.phase.litFraction.toFloat())
    drawShadow(centre, radius, blur = 14f, alpha = 0.6f * opacity * moon.phase.litFraction.toFloat())
    drawCircle(Color.White.copy(alpha = 0.07f * opacity), radius = radius, center = centre)
    scale(scaleX = if (moon.phase.waxing) 1f else -1f, scaleY = 1f, pivot = centre) {
        drawPath(litPart(centre, radius, moon.phase), Color(0xFFF3EFDF), alpha = opacity)
    }
}

private fun DrawScope.drawShadow(centre: Offset, radius: Float, blur: Float, alpha: Float) {
    val color = Color(245, 240, 215)
    val outer = radius + 3 * blur
    drawCircle(
        Brush.radialGradient(
            0f to color.copy(alpha = alpha),
            (radius - blur).coerceAtLeast(0f) / outer to color.copy(alpha = 0.84f * alpha),
            radius / outer to color.copy(alpha = 0.5f * alpha),
            (radius + blur) / outer to color.copy(alpha = 0.16f * alpha),
            (radius + 2 * blur) / outer to color.copy(alpha = 0.02f * alpha),
            1f to color.copy(alpha = 0f),
            center = centre,
            radius = outer,
        ),
        radius = outer,
        center = centre,
    )
}

private fun litPart(centre: Offset, radius: Float, phase: MoonPhase): Path {
    val terminator = radius * abs(1 - 2 * phase.litFraction.toFloat())
    val gibbous = phase.litFraction > 0.5
    return Path().apply {
        moveTo(centre.x, centre.y - radius)
        arcTo(Rect(centre, radius), startAngleDegrees = -90f, sweepAngleDegrees = 180f, forceMoveTo = false)
        arcTo(
            Rect(centre.x - terminator, centre.y - radius, centre.x + terminator, centre.y + radius),
            startAngleDegrees = 90f,
            sweepAngleDegrees = if (gibbous) 180f else -180f,
            forceMoveTo = false,
        )
        close()
    }
}
