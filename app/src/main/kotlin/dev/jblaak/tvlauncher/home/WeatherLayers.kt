package dev.jblaak.tvlauncher.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import dev.jblaak.tvlauncher.sky.Cloud
import dev.jblaak.tvlauncher.sky.Curtain
import dev.jblaak.tvlauncher.sky.Drop
import dev.jblaak.tvlauncher.sky.Flake
import dev.jblaak.tvlauncher.sky.Leaf
import dev.jblaak.tvlauncher.sky.Rgb
import dev.jblaak.tvlauncher.sky.Scud
import dev.jblaak.tvlauncher.sky.SkyWeather
import dev.jblaak.tvlauncher.sky.Star
import dev.jblaak.tvlauncher.sky.WeatherEffects
import dev.jblaak.tvlauncher.sky.skyScene
import dev.jblaak.tvlauncher.sky.typicalWind
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * The twinkling stars, drawn behind the moon.
 */
internal fun DrawScope.drawStars(stars: List<Star>, seconds: Float) {
    for (star in stars) {
        val twinkle = 0.15f + 0.75f * alternating(seconds, star.periodSeconds, star.phaseSeconds)
        drawCircle(Color.White.copy(alpha = twinkle), radius = star.size.toFloat() / 2, center = star.centre.toOffset())
    }
}

/**
 * The weather in front of the sun and moon, back to front as in the v1 mock-up (weatherFx), in reference pixels.
 * The blurs are drawn as soft gradients, which every Android version can draw.
 */
internal fun DrawScope.drawWeather(effects: WeatherEffects, seconds: Float) {
    if (effects.heatHaze) drawHeatHaze(seconds)
    effects.scud.forEach { drawScud(it, seconds) }
    effects.curtains.forEach { drawCurtain(it, seconds) }
    effects.leaves.forEach { drawLeaf(it, seconds) }
    effects.clouds.forEach { drawCloud(it, seconds) }
    effects.rain.forEach { drawDrop(it, seconds) }
    if (effects.lightning) drawLightning(seconds)
    effects.snow.forEach { drawFlake(it, seconds) }
}

/**
 * A warm band along the bottom that breathes: brighter and 12% taller, and back, every 10 s.
 */
private fun DrawScope.drawHeatHaze(seconds: Float) {
    val breath = alternating(seconds, 5.0, 0.0)
    scale(scaleX = 1f, scaleY = 1f + 0.12f * breath, pivot = Offset(640f, 720f)) {
        drawRect(
            Brush.verticalGradient(0f to Color.Transparent, 1f to Color(255, 140, 60).copy(alpha = 0.22f), startY = 400f, endY = 720f),
            topLeft = Offset(0f, 400f),
            size = Size(1280f, 320f),
            alpha = 0.55f + 0.45f * breath,
        )
    }
}

private fun DrawScope.drawScud(scud: Scud, seconds: Float) {
    val travelled = progress(seconds, scud.periodSeconds.toFloat(), scud.phaseSeconds.toFloat())
    val left = scud.fromX + (scud.toX - scud.fromX) * travelled
    drawSoftEllipse(
        topLeft = Offset(left.toFloat(), scud.top.toFloat()),
        size = Size(scud.width.toFloat(), scud.height.toFloat()),
        blur = 40f,
        color = Rgb(150, 145, 190).toColor(scud.opacity),
    )
}

/**
 * Vertical stripes of rain, 3 px every 17 px, faded out at both sides, slanted like the drops and swept across with
 * the wind. A curtain fades in over the first fifth of its sweep and out over the last.
 */
private fun DrawScope.drawCurtain(curtain: Curtain, seconds: Float) {
    val progress = progress(seconds, curtain.periodSeconds.toFloat(), curtain.phaseSeconds.toFloat())
    val opacity = easeIn(
        when {
            progress < 0.2f -> progress / 0.2f
            progress > 0.8f -> (1 - progress) / 0.2f
            else -> 1f
        },
    )
    val left = curtain.fromX.toFloat() + (curtain.toX - curtain.fromX).toFloat() * easeIn(progress)
    val lean = tan(Math.toRadians(curtain.skew)).toFloat() * 450f
    var stripe = 14f
    while (stripe < 1100f) {
        val across = (stripe + 1.5f) / 1100f
        val mask = when {
            across < 0.3f -> across / 0.3f
            across > 0.7f -> (1 - across) / 0.3f
            else -> 1f
        }
        val x = left + stripe + 1.5f
        drawLine(
            color = Color(205, 215, 240).copy(alpha = 0.07f * mask * opacity),
            start = Offset(x - lean, -80f),
            end = Offset(x + lean, 820f),
            strokeWidth = 3f,
        )
        stripe += 17f
    }
}

/**
 * A leaf tumbling along a fixed path of four points, mirrored when the wind blows to the left.
 */
private fun DrawScope.drawLeaf(leaf: Leaf, seconds: Float) {
    val progress = progress(seconds, leaf.periodSeconds.toFloat(), leaf.phaseSeconds.toFloat())
    val (x, y, degrees) = keyframes(
        progress,
        listOf(0f to Triple(-100f, 0f, 0f), 0.3f to Triple(380f, -70f, 300f), 0.6f to Triple(860f, 30f, 560f), 1f to Triple(1480f, -40f, 980f)),
    )
    val size = Size(leaf.width.toFloat(), leaf.height.toFloat())
    val topLeft = leaf.start.toOffset() + Offset(leaf.direction * x, y)
    rotate(leaf.direction * degrees, pivot = topLeft + Offset(size.width / 2, size.height / 2)) {
        val long = CornerRadius(0.8f * size.width, 0.8f * size.height)
        val short = CornerRadius(0.1f * size.width, 0.1f * size.height)
        val path = Path().apply {
            addRoundRect(RoundRect(topLeft.x, topLeft.y, topLeft.x + size.width, topLeft.y + size.height, long, short, long, short))
        }
        drawPath(path, leaf.color.toColor(0.9))
    }
}

private fun DrawScope.drawCloud(cloud: Cloud, seconds: Float) {
    val sway = -90f + 180f * alternating(seconds, cloud.periodSeconds, cloud.phaseSeconds)
    drawSoftEllipse(
        topLeft = cloud.topLeft.toOffset() + Offset(sway, 0f),
        size = Size(cloud.width.toFloat(), cloud.height.toFloat()),
        blur = cloud.blur.toFloat(),
        color = cloud.color.toColor(cloud.opacity),
    )
}

/**
 * A streak falling 1020 px while it drifts with the wind, fading in from its tail and leaning [Drop.tilt] degrees.
 */
private fun DrawScope.drawDrop(drop: Drop, seconds: Float) {
    val fallen = progress(seconds, drop.periodSeconds.toFloat(), drop.phaseSeconds.toFloat())
    val length = drop.length.toFloat()
    val centre = Offset(drop.startX.toFloat() + 0.75f + drop.drift.toFloat() * fallen, length / 2 - 140f + 1020f * fallen)
    val tilt = Math.toRadians(drop.tilt)
    val half = Offset(sin(tilt).toFloat(), cos(tilt).toFloat()) * (length / 2)
    val color = Color(215, 228, 255)
    drawLine(
        Brush.linearGradient(0f to color.copy(alpha = 0f), 1f to color.copy(alpha = drop.opacity.toFloat()), start = centre - half, end = centre + half),
        start = centre - half,
        end = centre + half,
        strokeWidth = 1.5f,
    )
}

/**
 * Every 9 s a double flash over the whole screen.
 */
private fun DrawScope.drawLightning(seconds: Float) {
    val flash = keyframes(
        progress(seconds, 9f, 0f),
        listOf(0f to 0f, 0.9f to 0f, 0.91f to 0.5f, 0.92f to 0.05f, 0.935f to 0f, 0.945f to 0.3f, 0.96f to 0f, 1f to 0f),
    )
    if (flash > 0f) drawRect(Color(225, 228, 255).copy(alpha = flash))
}

/**
 * A flake swaying down past a point 28 px right of the middle of its drift.
 */
private fun DrawScope.drawFlake(flake: Flake, seconds: Float) {
    val drift = flake.drift.toFloat()
    val (x, y) = keyframes(
        progress(seconds, flake.periodSeconds.toFloat(), flake.phaseSeconds.toFloat()),
        listOf(0f to Pair(0f, -30f), 0.5f to Pair(0.5f * drift + 28f, 380f), 1f to Pair(drift, 780f)),
    )
    val radius = flake.size.toFloat() / 2
    val centre = Offset(flake.startX.toFloat() + radius + x, radius + y)
    val color = Color.White.copy(alpha = flake.opacity.toFloat())
    if (flake.blurred) {
        val outer = radius + 3f
        drawCircle(Brush.radialGradient(0f to color, radius / outer * 0.6f to color, 1f to color.copy(alpha = 0f), center = centre, radius = outer), radius = outer, center = centre)
    } else {
        drawCircle(color, radius = radius, center = centre)
    }
}

/**
 * An ellipse in the box at [topLeft] blurred by about [blur] px: a radial gradient that reaches [blur] px beyond its
 * edge and fades over twice that across its narrow side.
 */
private fun DrawScope.drawSoftEllipse(topLeft: Offset, size: Size, blur: Float, color: Color) {
    val centre = topLeft + Offset(size.width / 2, size.height / 2)
    val radiusX = size.width / 2 + blur
    val radiusY = size.height / 2 + blur
    val solid = (1 - 2 * blur / minOf(radiusX, radiusY)).coerceAtLeast(0f)
    scale(scaleX = 1f, scaleY = radiusY / radiusX, pivot = centre) {
        drawCircle(
            Brush.radialGradient(0f to color, solid to color, 1f to color.copy(alpha = 0f), center = centre, radius = radiusX),
            radius = radiusX,
            center = centre,
        )
    }
}

/**
 * How far a CSS `alternate` animation with an ease-in-out curve is, 0..1..0: one way in [period] seconds and back.
 */
private fun alternating(seconds: Float, period: Double, lead: Double): Float {
    val cycle = (seconds + lead) / period
    val leg = (cycle - floor(cycle)).toFloat()
    val forward = floor(cycle).toLong() % 2 == 0L
    return easeInOut(if (forward) leg else 1 - leg)
}

private fun easeInOut(fraction: Float): Float = ((1 - cos(PI * fraction)) / 2).toFloat()

private fun easeIn(fraction: Float): Float = fraction * fraction

private fun keyframes(progress: Float, frames: List<Pair<Float, Float>>): Float {
    val (from, to) = frames.zipWithNext().first { (_, next) -> progress <= next.first }
    val fraction = (progress - from.first) / (to.first - from.first)
    return from.second + (to.second - from.second) * fraction
}

@JvmName("keyframesPairs")
private fun keyframes(progress: Float, frames: List<Pair<Float, Pair<Float, Float>>>): Pair<Float, Float> = Pair(
    keyframes(progress, frames.map { it.first to it.second.first }),
    keyframes(progress, frames.map { it.first to it.second.second }),
)

@JvmName("keyframesTriples")
private fun keyframes(progress: Float, frames: List<Pair<Float, Triple<Float, Float, Float>>>): Triple<Float, Float, Float> = Triple(
    keyframes(progress, frames.map { it.first to it.second.first }),
    keyframes(progress, frames.map { it.first to it.second.second }),
    keyframes(progress, frames.map { it.first to it.second.third }),
)

@Preview(device = "id:tv_1080p")
@Composable
internal fun WeatherEffectsPreview(
    @PreviewParameter(WeatherProvider::class) weather: SkyWeather,
) {
    val at = ZonedDateTime.of(2026, 10, 25, 22, 0, 0, 0, ZoneId.of("Europe/Amsterdam")).toInstant()
    val scene = skyScene(at, HomeConfig.Enschede.window, weather, typicalWind(weather))
    Canvas(modifier = Modifier.fillMaxSize().background(scene.colors.base.toColor(1.0))) {
        scale(size.width / 1280f, pivot = Offset.Zero) {
            drawStars(scene.effects.stars, seconds = 3.8f)
            drawWeather(scene.effects, seconds = 3.8f)
        }
    }
}

internal class WeatherProvider : PreviewParameterProvider<SkyWeather> {
    override val values = SkyWeather.entries.asSequence()
}
