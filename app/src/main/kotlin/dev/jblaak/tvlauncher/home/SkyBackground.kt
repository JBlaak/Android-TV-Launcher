package dev.jblaak.tvlauncher.home

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import dev.jblaak.tvlauncher.sky.Rgb
import dev.jblaak.tvlauncher.sky.ScreenPoint
import dev.jblaak.tvlauncher.sky.SkyColors
import dev.jblaak.tvlauncher.sky.SkyScene
import dev.jblaak.tvlauncher.sky.Sun
import kotlinx.coroutines.delay

/**
 * The living sky behind Home, drawn on one Canvas, with [content] on top. It animates at up to 30 fps, and only while
 * Home is resumed. If drawing ever fails, it falls back to the plain night-blue background for good.
 *
 * The same drawing is offered to [content] as [LocalSkyBackdrop], so frosted glass can show the sky behind it.
 */
@Composable
internal fun SkyBackground(
    scene: SkyScene,
    appGlow: Color,
    modifier: Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    var failed by remember { mutableStateOf(false) }
    val currentScene by rememberUpdatedState(scene)
    val glow by animateColorAsState(appGlow, tween(300), label = "app glow")
    val seconds = skyTime(animating = scene.sun is Sun.Disc || scene.life.shootingStars || scene.life.geese)
    val sunPaths = remember { SunPaths() }
    val backdrop = remember {
        SkyBackdrop {
            if (failed) return@SkyBackdrop
            @Suppress("TooGenericExceptionCaught")
            try {
                drawSky(currentScene.colors, glow)
                drawSun(currentScene.sun, sunPaths, seconds.value)
                drawSkyLife(currentScene.life, seconds.value)
            } catch (exception: RuntimeException) {
                Log.e("SkyBackground", "The sky failed to draw, falling back to a plain background", exception)
                failed = true
            }
        }
    }
    Box(modifier = modifier.background(Color(0xFF101224))) {
        if (!failed) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                scale(size.width / 1280f, pivot = Offset.Zero) {
                    with(backdrop) { drawReference() }
                }
            }
        }
        CompositionLocalProvider(LocalSkyBackdrop provides backdrop) {
            content()
        }
    }
}

/**
 * The sky as glass sees it: the same layers as [SkyBackground], in 1280×720 reference pixels.
 */
internal fun interface SkyBackdrop {
    fun DrawScope.drawReference()
}

/**
 * The sky behind the content, or null outside a [SkyBackground].
 */
internal val LocalSkyBackdrop = staticCompositionLocalOf<SkyBackdrop?> { null }

/**
 * Seconds of animation since Home first showed, advanced by frames at most 30 times a second while Home is
 * resumed. Time stands still while another app is in front.
 */
@Composable
private fun skyTime(animating: Boolean): State<Float> {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    return produceState(0f, animating, lifecycle) {
        if (!animating) return@produceState
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            var previous = withFrameNanos { it }
            while (true) {
                delay(20)
                val frame = withFrameNanos { it }
                value += ((frame - previous) / 1e9f).coerceAtMost(0.1f)
                previous = frame
            }
        }
    }
}

/**
 * Back to front: the base colour, the cool glow, the warm glow and the focused app's glow from below
 * (radial gradients sized in % of the 1280×720 reference, as in the v1 mock-up).
 */
private fun DrawScope.drawSky(colors: SkyColors, appGlow: Color) {
    drawRect(colors.base.toColor(1.0))
    drawEllipticalGlow(
        centre = colors.coolCentre.toOffset(),
        radiusX = 0.45f * 1280,
        radiusY = 0.55f * 720,
        color = colors.cool.toColor(alpha = colors.strength * (0.25 + 0.6 * GLOW)),
        fadeOutAt = 0.65f,
    )
    drawEllipticalGlow(
        centre = colors.warmCentre.toOffset(),
        radiusX = 0.6f * 1280,
        radiusY = 0.7f * 720,
        color = colors.warm.toColor(alpha = colors.strength * (0.4 + GLOW)),
        fadeOutAt = 0.65f,
    )
    drawEllipticalGlow(
        centre = Offset(640f, 1.2f * 720),
        radiusX = 0.7f * 1280,
        radiusY = 0.75f * 720,
        color = appGlow.copy(alpha = 0.75f * GLOW.toFloat()),
        fadeOutAt = 0.7f,
    )
}

private fun DrawScope.drawEllipticalGlow(centre: Offset, radiusX: Float, radiusY: Float, color: Color, fadeOutAt: Float) {
    scale(scaleX = 1f, scaleY = radiusY / radiusX, pivot = centre) {
        val brush = Brush.radialGradient(0f to color, fadeOutAt to color.copy(alpha = 0f), center = centre, radius = radiusX)
        drawCircle(brush, radius = radiusX, center = centre)
    }
}

internal fun Rgb.toColor(alpha: Double): Color = Color(red, green, blue, (alpha.coerceIn(0.0, 1.0) * 255).toInt())

internal fun ScreenPoint.toOffset(): Offset = Offset(x.toFloat(), y.toFloat())

private const val GLOW = 0.6

@Preview(device = "id:tv_1080p")
@Composable
internal fun SkyBackgroundPreview() {
    SkyBackground(scene = HomeState.previewValue.sky, appGlow = Color(0xFF4AA83F), modifier = Modifier.fillMaxSize()) {}
}
