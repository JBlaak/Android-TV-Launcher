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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import dev.jblaak.tvlauncher.sky.Bead
import dev.jblaak.tvlauncher.sky.SkyWeather
import dev.jblaak.tvlauncher.sky.Slide
import dev.jblaak.tvlauncher.sky.WindowGlass
import dev.jblaak.tvlauncher.sky.skyScene
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.hypot

/**
 * Rain beads or frost on the window pane, in front of everything else in the sky (canvas copy, DockNight whimsyFx),
 * in reference pixels.
 */
internal fun DrawScope.drawWindowGlass(glass: WindowGlass, seconds: Float) {
    if (glass.frost) drawFrost()
    glass.beads.forEach { drawBead(it, seconds) }
}

/**
 * Frost creeping in from the bottom corners, and a little from the top left.
 */
private fun DrawScope.drawFrost() {
    val frost = Color(230, 240, 255)
    drawEllipticalGlow(Offset(0f, 720f), radiusX = 0.4f * 1280, radiusY = 0.5f * 720, color = frost.copy(alpha = 0.16f), fadeOutAt = 0.7f)
    drawEllipticalGlow(Offset(1280f, 720f), radiusX = 0.35f * 1280, radiusY = 0.45f * 720, color = frost.copy(alpha = 0.14f), fadeOutAt = 0.7f)
    drawEllipticalGlow(Offset.Zero, radiusX = 0.3f * 1280, radiusY = 0.35f * 720, color = frost.copy(alpha = 0.10f), fadeOutAt = 0.7f)
}

/**
 * A bead: a thin shadow under its lower edge, a clear body with a bright highlight in its upper left, and light caught along
 * its lower rim.
 */
private fun DrawScope.drawBead(bead: Bead, seconds: Float) {
    val size = Size(bead.width.toFloat(), bead.height.toFloat())
    val topLeft = bead.topLeft.toOffset() + (bead.slide?.let { slid(it, seconds) } ?: Offset.Zero)
    drawLowerRim(Color.Black.copy(alpha = 0.25f), topLeft + Offset(0f, 1f), size, width = 1.5f)
    val white = Color.White
    drawOval(
        Brush.radialGradient(
            0f to white.copy(alpha = 0.6f),
            0.12f to white.copy(alpha = 0.6f),
            0.4f to white.copy(alpha = 0.1f),
            0.7f to white.copy(alpha = 0.03f),
            center = topLeft + Offset(0.36f * size.width, 0.3f * size.height),
            radius = hypot(0.64f * size.width, 0.7f * size.height),
        ),
        topLeft = topLeft,
        size = size,
    )
    drawLowerRim(white.copy(alpha = 0.28f), topLeft + Offset(0.5f, 0.5f), Size(size.width - 1f, size.height - 1f), width = 1f)
}

private fun DrawScope.drawLowerRim(color: Color, topLeft: Offset, size: Size, width: Float) {
    drawArc(color, startAngle = 20f, sweepAngle = 140f, useCenter = false, topLeft = topLeft, size = size, style = Stroke(width = width))
}

/**
 * How far a sliding bead has moved: still for the first half of its loop, creeping 40 px down by 62%, then speeding
 * up to 820 px down by the end, each leg eased in.
 */
private fun slid(slide: Slide, seconds: Float): Offset {
    val progress = progress(seconds, slide.periodSeconds.toFloat(), slide.phaseSeconds.toFloat())
    return when {
        progress < 0.5f -> Offset.Zero
        progress < 0.62f -> Offset(1f, 40f) * easeIn((progress - 0.5f) / 0.12f)
        else -> Offset(1f, 40f) + Offset(-4f, 780f) * easeIn((progress - 0.62f) / 0.38f)
    }
}

private fun easeIn(fraction: Float): Float = fraction * fraction

@Preview(device = "id:tv_1080p")
@Composable
internal fun WindowGlassPreview(
    @PreviewParameter(GlassWeatherProvider::class) weather: SkyWeather,
) {
    val at = ZonedDateTime.of(2026, 10, 25, 17, 5, 0, 0, ZoneId.of("Europe/Amsterdam")).toInstant()
    val scene = skyScene(at, HomeConfig.Enschede.window, weather)
    Canvas(modifier = Modifier.fillMaxSize().background(scene.colors.base.toColor(1.0))) {
        scale(size.width / 1280f, pivot = Offset.Zero) {
            drawWindowGlass(scene.glass, seconds = 8f)
        }
    }
}

internal class GlassWeatherProvider : PreviewParameterProvider<SkyWeather> {
    override val values = sequenceOf(SkyWeather.Rain, SkyWeather.Snow)
}
