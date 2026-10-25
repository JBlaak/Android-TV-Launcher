package dev.jblaak.tvlauncher.home

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import dev.jblaak.tvlauncher.sky.Burst
import dev.jblaak.tvlauncher.sky.Flag
import dev.jblaak.tvlauncher.sky.bunting
import dev.jblaak.tvlauncher.sky.fireworks
import kotlin.math.floor
import kotlin.math.sqrt

internal class CelebrationLayers(celebration: Celebration) {
    val bursts: List<Burst> = if (celebration == Celebration.Fireworks) fireworks() else emptyList()
    val flags: List<Flag> = if (celebration == Celebration.Bunting) bunting() else emptyList()
    val flag: Path = Path().apply {
        moveTo(-15f, 0f)
        lineTo(15f, 0f)
        lineTo(0f, 34f)
        close()
    }
}

internal fun DrawScope.drawCelebration(layers: CelebrationLayers, seconds: Float) {
    layers.bursts.forEach { drawBurst(it, seconds) }
    layers.flags.forEach { drawFlag(it, layers.flag, seconds) }
}

private fun DrawScope.drawBurst(burst: Burst, seconds: Float) {
    val period = burst.periodSeconds.toFloat()
    val progress = (seconds + burst.phaseSeconds.toFloat()).mod(period) / period
    val centre = burst.centre.toOffset()
    val color = burst.color.toColor(1.0)
    val bloomScale = if (progress < 0.45f) 0.3f + 0.9f * EaseOut.transform(progress / 0.45f) else 1.2f
    val bloomOpacity = when {
        progress < 0.08f -> EaseOut.transform(progress / 0.08f)
        progress < 0.45f -> 1f - EaseOut.transform((progress - 0.08f) / 0.37f)
        else -> 0f
    }
    if (bloomOpacity > 0f) {
        val radius = 120f * bloomScale
        drawCircle(
            Brush.radialGradient(
                0f to color.copy(alpha = 0.35f * bloomOpacity),
                0.65f to color.copy(alpha = 0f),
                center = centre,
                radius = radius * sqrt(2f),
            ),
            radius = radius,
            center = centre,
        )
    }
    val travelled = SparkEasing.transform(progress)
    val sparkOpacity = when {
        progress < 0.06f -> SparkEasing.transform(progress / 0.06f)
        progress < 0.75f -> 1f - 0.1f * SparkEasing.transform((progress - 0.06f) / 0.69f)
        else -> 0.9f * (1f - SparkEasing.transform((progress - 0.75f) / 0.25f))
    }
    burst.sparks.forEach { spark ->
        val position = centre + Offset(spark.x.toFloat(), spark.y.toFloat() + 60f) * travelled
        drawCircle(color.copy(alpha = 0.3f * sparkOpacity), radius = 5f, center = position)
        drawCircle(color.copy(alpha = sparkOpacity), radius = 2f, center = position)
    }
}

private fun DrawScope.drawFlag(flag: Flag, shape: Path, seconds: Float) {
    val time = seconds + flag.phaseSeconds.toFloat()
    val sway = flag.swaySeconds.toFloat()
    val eased = EaseInOut.transform(time.mod(sway) / sway)
    val swing = if (floor(time / sway).toInt() % 2 == 0) -5f + 10f * eased else 5f - 10f * eased
    translate(flag.anchor.x.toFloat(), flag.anchor.y.toFloat()) {
        rotate(flag.rotation.toFloat() + swing, pivot = Offset.Zero) {
            drawPath(shape, flag.color.toColor(0.92))
        }
    }
}

private val SparkEasing = CubicBezierEasing(0.15f, 0.7f, 0.3f, 1f)
