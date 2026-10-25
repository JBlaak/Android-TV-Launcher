package dev.jblaak.tvlauncher.sky

import dev.jblaak.tvlauncher.sky.internal.Horizontal
import dev.jblaak.tvlauncher.sky.internal.WindowPosition
import dev.jblaak.tvlauncher.sky.internal.moonPhase
import dev.jblaak.tvlauncher.sky.internal.moonPosition
import dev.jblaak.tvlauncher.sky.internal.project
import dev.jblaak.tvlauncher.sky.internal.skyKeyframe
import dev.jblaak.tvlauncher.sky.internal.skyLife
import dev.jblaak.tvlauncher.sky.internal.sunPosition
import dev.jblaak.tvlauncher.sky.internal.tinted
import java.time.Instant
import kotlin.math.min

fun skyScene(at: Instant, window: Window, weather: SkyWeather): SkyScene {
    val sun = sunPosition(at, window.location)
    val onScreen = project(sun, window.facing)
    return SkyScene(
        colors = skyColors(sun, onScreen, weather),
        sun = sun(sun, onScreen, weather),
        moon = moon(at, window, weather, sunUp = sun.elevation > 0),
        life = skyLife(at, sun.elevation, weather),
    )
}

/**
 * The moon shows when it's up, in view and at least 3% lit. Clear skies show the disc, paler while the sun is up;
 * clouds dim it; rain, storm, snow and fog leave only its glow, and only at night.
 */
private fun moon(at: Instant, window: Window, weather: SkyWeather, sunUp: Boolean): Moon {
    val moon = moonPosition(at, window.location)
    val onScreen = project(moon, window.facing)
    val phase = moonPhase(at)
    if (moon.elevation <= 0 || onScreen.visibility <= 0 || phase.litFraction <= 0.03) return Moon.Hidden
    return when (weather) {
        SkyWeather.Clear, SkyWeather.Warm, SkyWeather.Clouds -> Moon.Disc(
            position = onScreen.point,
            opacity = (if (sunUp) 0.55 else 1.0) * (if (weather == SkyWeather.Clouds) 0.45 else 1.0) * onScreen.visibility,
            phase = phase,
        )

        SkyWeather.Rain, SkyWeather.Storm, SkyWeather.Snow, SkyWeather.Fog -> if (sunUp) {
            Moon.Hidden
        } else {
            Moon.Glow(position = onScreen.point, opacity = 0.14 * phase.litFraction * onScreen.visibility)
        }
    }
}

/**
 * The warm glow sits at the sun's position, held within reach of the screen; the cool glow mirrors it near the top
 * (v1 mock-up: gx, gy).
 */
private fun skyColors(sun: Horizontal, onScreen: WindowPosition, weather: SkyWeather): SkyColors {
    val keyframe = skyKeyframe(sun.elevation).tinted(weather)
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

/**
 * Clear or warm weather shows the sun itself, a bit brighter when it's warm. Clouds, fog and rain soften it to a
 * diffuse glow while it's up and in view; a storm or snow hides it (v1 mock-up: weatherFx).
 */
private fun sun(sun: Horizontal, onScreen: WindowPosition, weather: SkyWeather): Sun = when (weather) {
    SkyWeather.Clear, SkyWeather.Warm -> clearSun(sun, onScreen, brightness = if (weather == SkyWeather.Warm) 1.15 else 1.0)
    SkyWeather.Clouds, SkyWeather.Fog -> veiledSun(sun, onScreen, opacity = 0.18)
    SkyWeather.Rain -> veiledSun(sun, onScreen, opacity = 0.08)
    SkyWeather.Storm, SkyWeather.Snow -> if (sun.elevation > 0) Sun.Hidden else Sun.BelowHorizon
}

private fun clearSun(sun: Horizontal, onScreen: WindowPosition, brightness: Double): Sun = when {
    sun.elevation > -1 && onScreen.visibility > 0 -> Sun.Disc(
        position = onScreen.point,
        intensity = onScreen.visibility * ((sun.elevation + 1) / 7).coerceIn(0.35, 1.0) * brightness,
        coreOpacity = min(1.0, 1.2 * onScreen.visibility),
        colors = sunColors(sun.elevation),
    )

    sun.elevation > 0 -> Sun.BehindViewer

    else -> Sun.BelowHorizon
}

private fun veiledSun(sun: Horizontal, onScreen: WindowPosition, opacity: Double): Sun = when {
    sun.elevation > 0 && onScreen.visibility > 0 -> Sun.Diffuse(position = onScreen.point, opacity = opacity * onScreen.visibility)
    sun.elevation > 0 -> Sun.Hidden
    else -> Sun.BelowHorizon
}

private fun sunColors(elevation: Double): SunColors = when {
    elevation < 3 -> SunColors(glow = Rgb(255, 120, 50), core = Rgb(255, 214, 160))
    elevation < 12 -> SunColors(glow = Rgb(255, 170, 85), core = Rgb(255, 240, 212))
    else -> SunColors(glow = Rgb(255, 228, 170), core = Rgb(255, 252, 244))
}
