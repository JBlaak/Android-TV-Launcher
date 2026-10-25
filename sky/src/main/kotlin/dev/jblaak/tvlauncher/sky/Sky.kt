package dev.jblaak.tvlauncher.sky

import dev.jblaak.tvlauncher.sky.internal.Horizontal
import dev.jblaak.tvlauncher.sky.internal.SkyWeather
import dev.jblaak.tvlauncher.sky.internal.WindowPosition
import dev.jblaak.tvlauncher.sky.internal.project
import dev.jblaak.tvlauncher.sky.internal.skyKeyframe
import dev.jblaak.tvlauncher.sky.internal.skyLife
import dev.jblaak.tvlauncher.sky.internal.sunPosition
import java.time.Instant
import kotlin.math.min

fun skyScene(at: Instant, window: Window): SkyScene = skyScene(at, window, SkyWeather.Clear)

internal fun skyScene(at: Instant, window: Window, weather: SkyWeather): SkyScene {
    val sun = sunPosition(at, window.location)
    val onScreen = project(sun, window.facing)
    return SkyScene(
        colors = skyColors(sun, onScreen),
        sun = sun(sun, onScreen),
        life = skyLife(at, sun.elevation, weather),
    )
}

/**
 * The warm glow sits at the sun's position, held within reach of the screen; the cool glow mirrors it near the top
 * (v1 mock-up: gx, gy).
 */
private fun skyColors(sun: Horizontal, onScreen: WindowPosition): SkyColors {
    val keyframe = skyKeyframe(sun.elevation)
    val warmX = onScreen.point.x.coerceIn(-64.0, 1344.0)
    val warmY = onScreen.point.y.coerceIn(0.0, 648.0)
    return SkyColors(
        base = keyframe.base,
        warm = keyframe.warm,
        warmCentre = ScreenPoint(warmX, warmY),
        cool = keyframe.cool,
        coolCentre = ScreenPoint(1280 - warmX, 36.0),
        strength = keyframe.strength,
    )
}

private fun sun(sun: Horizontal, onScreen: WindowPosition): Sun = when {
    sun.elevation > -1 && onScreen.visibility > 0 -> Sun.Disc(
        position = onScreen.point,
        intensity = onScreen.visibility * ((sun.elevation + 1) / 7).coerceIn(0.35, 1.0),
        coreOpacity = min(1.0, 1.2 * onScreen.visibility),
        colors = sunColors(sun.elevation),
    )

    sun.elevation > 0 -> Sun.BehindViewer

    else -> Sun.BelowHorizon
}

private fun sunColors(elevation: Double): SunColors = when {
    elevation < 3 -> SunColors(glow = Rgb(255, 120, 50), core = Rgb(255, 214, 160))
    elevation < 12 -> SunColors(glow = Rgb(255, 170, 85), core = Rgb(255, 240, 212))
    else -> SunColors(glow = Rgb(255, 228, 170), core = Rgb(255, 252, 244))
}
