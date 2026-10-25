package dev.jblaak.tvlauncher.home

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import dev.jblaak.tvlauncher.sky.Sun
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The ray and ghost shapes, built once and reused every frame. Ray paths are centred on the origin.
 */
internal class SunPaths {
    val raysA: Path = rays(firstDegrees = 3f, widthDegrees = 1f, stepDegrees = 7.5f, radius = 380f)
    val raysB: Path = rays(firstDegrees = 0f, widthDegrees = 2.5f, stepDegrees = 17f, radius = 260f)
}

/**
 * The sun as in the v1 mock-up (sunFx), in reference pixels. CSS radial gradients reach the box's farthest corner,
 * so a gradient on a round box of radius r runs to r·√2.
 */
internal fun DrawScope.drawSun(sun: Sun, paths: SunPaths, seconds: Float) {
    when (sun) {
        is Sun.Disc -> drawDisc(sun, paths, seconds)

        Sun.BehindViewer -> drawRect(
            Brush.verticalGradient(0f to Color(255, 226, 175).copy(alpha = 0.12f), 0.5f to Color(255, 226, 175).copy(alpha = 0f)),
        )

        Sun.BelowHorizon -> Unit
    }
}

private fun DrawScope.drawDisc(sun: Sun.Disc, paths: SunPaths, seconds: Float) {
    val centre = sun.position.toOffset()
    val k = sun.intensity.toFloat()
    val glow = sun.colors.glow.toColor(1.0)
    val core = sun.colors.core.toColor(1.0)
    drawRoundGradient(centre, 550f, 0f to glow.withAlpha(0.5f * k), 0.22f to glow.withAlpha(0.16f * k), 0.6f to glow.withAlpha(0f))
    drawRays(paths.raysA, centre, 360f * seconds / 260f, Color(255, 248, 230).withAlpha(0.2f * k), 380f, 0.52f)
    drawRays(paths.raysB, centre, -360f * seconds / 180f, Color(255, 240, 215).withAlpha(0.12f * k), 260f, 0.55f)
    drawRoundGradient(
        centre,
        190f,
        0.57f to Color(255, 226, 180).withAlpha(0f),
        0.62f to Color(255, 226, 180).withAlpha(0.12f * k),
        0.66f to Color(170, 215, 255).withAlpha(0.08f * k),
        0.71f to Color(170, 215, 255).withAlpha(0f),
    )
    drawCore(centre, sun.coreOpacity.toFloat(), k, glow, core)
    drawStreak(centre, k)
    drawGhosts(centre, k)
}

/**
 * The hot core with its two CSS box-shadows (blur 40 spread 16, blur 140 spread 50), approximated by radial fades
 * from the inner to the outer edge of each blur.
 */
private fun DrawScope.drawCore(centre: Offset, opacity: Float, k: Float, glow: Color, core: Color) {
    drawShadow(centre, radius = 35f + 50f, blur = 140f, color = glow.withAlpha(0.35f * k * opacity))
    drawShadow(centre, radius = 35f + 16f, blur = 40f, color = core.withAlpha(0.75f * k * opacity))
    val radius = 35f * sqrt(2f)
    drawCircle(
        Brush.radialGradient(
            0f to Color.White,
            0.42f to Color.White,
            0.58f to core,
            1f to core.withAlpha(0f),
            center = centre,
            radius = radius,
        ),
        radius = 35f,
        center = centre,
        alpha = opacity,
    )
}

private fun DrawScope.drawShadow(centre: Offset, radius: Float, blur: Float, color: Color) {
    val outer = radius + blur / 2
    drawCircle(
        Brush.radialGradient(
            0f to color,
            (radius - blur / 2).coerceAtLeast(0f) / outer to color,
            radius / outer to color.withAlpha(color.alpha / 2),
            1f to color.withAlpha(0f),
            center = centre,
            radius = outer,
        ),
        radius = outer,
        center = centre,
    )
}

private fun DrawScope.drawStreak(centre: Offset, k: Float) {
    val blue = Color(150, 190, 255)
    val brush = Brush.horizontalGradient(
        0f to blue.withAlpha(0f),
        0.3f to blue.withAlpha(0.22f * k),
        0.5f to Color.White.withAlpha(0.55f * k),
        0.7f to blue.withAlpha(0.22f * k),
        1f to blue.withAlpha(0f),
        startX = centre.x - 520f,
        endX = centre.x + 520f,
    )
    drawOval(brush, topLeft = Offset(centre.x - 520f, centre.y - 4f), size = Size(1040f, 8f), alpha = 0.5f, blendMode = BlendMode.Screen)
    drawOval(brush, topLeft = Offset(centre.x - 520f, centre.y - 2.5f), size = Size(1040f, 5f), blendMode = BlendMode.Screen)
}

/**
 * Seven lens-flare ghosts on the line from the sun through the centre of the screen, at fractions of that line.
 */
private fun DrawScope.drawGhosts(sun: Offset, k: Float) {
    val toCentre = Offset(640f, 360f) - sun
    fun at(t: Float) = sun + toCentre * t
    drawCircle(Color(255, 214, 140).withAlpha(0.22f * k), radius = 17f, center = at(0.42f), blendMode = BlendMode.Screen)
    drawHexagon(at(0.78f), 96f, Color(120, 200, 255).withAlpha(0.09f * k))
    drawCircle(Color.White.withAlpha(0.25f * k), radius = 10f, center = at(1.08f), blendMode = BlendMode.Screen)
    val pink = Color(255, 140, 220)
    drawRoundGradient(at(1.36f), 75f, 0f to pink.withAlpha(0.03f * k), 0.7f to pink.withAlpha(0.1f * k), 0.72f to pink.withAlpha(0f))
    drawHexagon(at(1.62f), 58f, Color(140, 255, 205).withAlpha(0.1f * k))
    drawRoundGradient(
        at(1.95f),
        120f,
        0.6f to Color(255, 190, 120).withAlpha(0f),
        0.66f to Color(255, 190, 120).withAlpha(0.08f * k),
        0.7f to Color(140, 200, 255).withAlpha(0.06f * k),
        0.74f to Color(140, 200, 255).withAlpha(0f),
    )
    drawCircle(Color(200, 165, 255).withAlpha(0.12f * k), radius = 22f, center = at(2.25f), blendMode = BlendMode.Screen)
}

private fun DrawScope.drawHexagon(centre: Offset, size: Float, color: Color) {
    val corners = listOf(0.25f to 0.05f, 0.75f to 0.05f, 1f to 0.5f, 0.75f to 0.95f, 0.25f to 0.95f, 0f to 0.5f)
    val hexagon = Path().apply {
        corners.forEachIndexed { index, (x, y) ->
            val point = centre + Offset((x - 0.5f) * size, (y - 0.5f) * size)
            if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
        }
        close()
    }
    drawPath(hexagon, color, blendMode = BlendMode.Screen)
}

/**
 * A round box of [radius] filled with a CSS `radial-gradient(circle, …)`, whose stops run to the farthest corner.
 */
private fun DrawScope.drawRoundGradient(centre: Offset, radius: Float, vararg stops: Pair<Float, Color>) {
    drawCircle(
        Brush.radialGradient(*stops, center = centre, radius = radius * sqrt(2f)),
        radius = radius,
        center = centre,
        blendMode = BlendMode.Screen,
    )
}

/**
 * A repeating conic gradient of rays, masked by a radial fade that ends at [fadeOutAt] of the farthest corner.
 */
private fun DrawScope.drawRays(rays: Path, centre: Offset, degrees: Float, color: Color, radius: Float, fadeOutAt: Float) {
    translate(centre.x, centre.y) {
        rotate(degrees, pivot = Offset.Zero) {
            val brush = Brush.radialGradient(0f to color, fadeOutAt to color.withAlpha(0f), center = Offset.Zero, radius = radius * sqrt(2f))
            drawPath(rays, brush, blendMode = BlendMode.Screen)
        }
    }
}

/**
 * Wedges from the origin, as CSS draws a conic gradient: clockwise from the top.
 */
private fun rays(firstDegrees: Float, widthDegrees: Float, stepDegrees: Float, radius: Float): Path = Path().apply {
    var start = firstDegrees
    while (start < firstDegrees + 360f) {
        val end = minOf(start + widthDegrees, firstDegrees + 360f)
        moveTo(0f, 0f)
        lineTo(radius * sin(start.radians()), -radius * cos(start.radians()))
        lineTo(radius * sin(end.radians()), -radius * cos(end.radians()))
        close()
        start += stepDegrees
    }
}

private fun Float.radians(): Float = (this * PI / 180).toFloat()

private fun Color.withAlpha(alpha: Float): Color = copy(alpha = alpha.coerceIn(0f, 1f))
