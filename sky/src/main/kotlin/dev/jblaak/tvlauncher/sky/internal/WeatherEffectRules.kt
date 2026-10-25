package dev.jblaak.tvlauncher.sky.internal

import dev.jblaak.tvlauncher.sky.Cloud
import dev.jblaak.tvlauncher.sky.Curtain
import dev.jblaak.tvlauncher.sky.Drop
import dev.jblaak.tvlauncher.sky.Flake
import dev.jblaak.tvlauncher.sky.Leaf
import dev.jblaak.tvlauncher.sky.Rgb
import dev.jblaak.tvlauncher.sky.ScreenPoint
import dev.jblaak.tvlauncher.sky.Scud
import dev.jblaak.tvlauncher.sky.SkyWeather
import dev.jblaak.tvlauncher.sky.Star
import dev.jblaak.tvlauncher.sky.WeatherEffects
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.tan

/**
 * The weather's particles with the mock-up's seeds (v1 mock-up: weatherFx). Android starts with about 40% fewer stars,
 * drops and flakes than the mock-up; the few large clouds, curtains and leaves keep their count.
 */
internal fun weatherEffects(weather: SkyWeather, sunElevation: Double, wind: WindOnScreen): WeatherEffects {
    val clearSky = weather == SkyWeather.Clear || weather == SkyWeather.Warm
    val storm = weather == SkyWeather.Storm
    return WeatherEffects(
        stars = if (clearSky && sunElevation < -6) stars() else emptyList(),
        heatHaze = weather == SkyWeather.Warm,
        scud = if (storm) scud(wind) else emptyList(),
        curtains = if (storm) curtains(wind) else emptyList(),
        leaves = if (storm && abs(wind.side) > 20) leaves(wind) else emptyList(),
        clouds = when (weather) {
            SkyWeather.Clouds -> clouds(count = 7, spread = 420.0, color = Rgb(220, 226, 236))
            SkyWeather.Rain -> clouds(count = 5, spread = 220.0, color = Rgb(220, 226, 236))
            SkyWeather.Snow -> clouds(count = 5, spread = 220.0, color = Rgb(235, 240, 255))
            SkyWeather.Fog -> fogBands()
            SkyWeather.Clear, SkyWeather.Warm, SkyWeather.Storm -> emptyList()
        },
        rain = when (weather) {
            SkyWeather.Rain -> rain(count = 54, basePeriod = 0.65, wind = wind, gusty = false)
            SkyWeather.Storm -> rain(count = 90, basePeriod = 0.45, wind = wind, gusty = true)
            else -> emptyList()
        },
        lightning = storm,
        snow = if (weather == SkyWeather.Snow) snow(wind) else emptyList(),
    )
}

/**
 * Stars across the top 62% of the sky.
 */
private fun stars(): List<Star> = List(42) { i ->
    val size = 1 + seeded(i * 7) * 2
    Star(
        centre = ScreenPoint(seeded(i * 7 + 1) * 1280 + size / 2, seeded(i * 7 + 2) * 0.62 * 720 + size / 2),
        size = size,
        periodSeconds = 2 + seeded(i * 7 + 3) * 3,
        phaseSeconds = seeded(i * 7 + 4) * 5,
    )
}

/**
 * Faster wind, faster clouds: `pace = 60 / max(20, |side| + 0.3 · speed)` scales each cloud's crossing time.
 */
private fun scud(wind: WindOnScreen): List<Scud> {
    val pace = 60 / max(20.0, abs(wind.side) + wind.speed * 0.3)
    return List(9) { i ->
        Scud(
            top = -160 + seeded(i * 5 + 400) * 360,
            width = 560 + seeded(i * 5 + 401) * 460,
            height = 170 + seeded(i * 5 + 402) * 150,
            opacity = 0.08 + seeded(i * 5 + 403) * 0.1,
            fromX = if (wind.direction > 0) -900.0 else 1700.0,
            toX = if (wind.direction > 0) 1700.0 else -900.0,
            periodSeconds = (12 + seeded(i * 5 + 404) * 12) * pace,
            phaseSeconds = seeded(i * 5 + 405) * 24 * pace,
        )
    }
}

private fun curtains(wind: WindOnScreen): List<Curtain> = List(3) { i ->
    Curtain(
        fromX = if (wind.direction > 0) -1500.0 else 1500.0,
        toX = if (wind.direction > 0) 1500.0 else -1500.0,
        skew = wind.tilt(1.0),
        periodSeconds = 3.5 + i * 1.3,
        phaseSeconds = i * 2.1,
    )
}

private fun leaves(wind: WindOnScreen): List<Leaf> {
    val tones = listOf(Rgb(176, 122, 58), Rgb(140, 96, 44), Rgb(196, 150, 72), Rgb(120, 110, 52))
    return List(7) { i ->
        Leaf(
            start = ScreenPoint(if (wind.direction > 0) 0.0 else 1280.0, (12 + seeded(i * 3 + 500) * 62) / 100 * 720),
            width = 9 + seeded(i * 3 + 501) * 7,
            height = 5 + seeded(i * 3 + 502) * 3,
            color = tones[i % tones.size],
            direction = wind.direction,
            periodSeconds = 2.6 + seeded(i * 3 + 503) * 2.4,
            phaseSeconds = seeded(i * 3 + 504) * 6,
        )
    }
}

private fun clouds(count: Int, spread: Double, color: Rgb): List<Cloud> = List(count) { i ->
    Cloud(
        topLeft = ScreenPoint((seeded(i * 5 + 50) * 90 - 20) / 100 * 1280, -140 + seeded(i * 5 + 51) * spread),
        width = 520 + seeded(i * 5 + 52) * 420,
        height = 180 + seeded(i * 5 + 53) * 140,
        blur = 48.0,
        color = color,
        opacity = 0.07 + seeded(i * 5 + 54) * 0.08,
        periodSeconds = 40 + seeded(i * 5 + 55) * 40,
        phaseSeconds = seeded(i) * 40,
    )
}

private fun fogBands(): List<Cloud> = List(5) { i ->
    Cloud(
        topLeft = ScreenPoint(-0.2 * 1280, (18 + i * 16) / 100.0 * 720),
        width = 1.4 * 1280,
        height = 120 + seeded(i + 90) * 100,
        blur = 34.0,
        color = Rgb(220, 225, 235),
        opacity = 0.1 + seeded(i + 91) * 0.08,
        periodSeconds = 30 + seeded(i + 92) * 30,
        phaseSeconds = seeded(i + 93) * 30,
    )
}

/**
 * Drops travel `1020 · tan(tilt)` px sideways while they fall, and their start points spread so slanted rain still
 * covers the whole screen. When [gusty], every third drop rides the gusts at 1.5× the wind.
 */
private fun rain(count: Int, basePeriod: Double, wind: WindOnScreen, gusty: Boolean): List<Drop> = List(count) { i ->
    val tilt = wind.tilt(if (gusty && i % 3 == 0) 1.5 else 1.0)
    val drift = 1020 * tan(Math.toRadians(tilt))
    Drop(
        startX = -max(0.0, drift) + seeded(i * 3 + 200) * (1280 + abs(drift)),
        length = 50 + seeded(i * 3 + 201) * 60,
        opacity = 0.25 + seeded(i * 3 + 202) * 0.4,
        tilt = tilt,
        drift = drift,
        periodSeconds = basePeriod + seeded(i * 3 + 203) * 0.3,
        phaseSeconds = seeded(i * 3 + 204) * 2,
    )
}

/**
 * Flakes start shifted against the wind by `0.45 · side` % of the width, so they drift back across the screen.
 */
private fun snow(wind: WindOnScreen): List<Flake> = List(48) { i ->
    val size = 3 + seeded(i * 4 + 300) * 7
    Flake(
        startX = (seeded(i * 4 + 301) * 100 - wind.side * 0.45) / 100 * 1280,
        size = size,
        opacity = 0.5 + seeded(i * 4 + 302) * 0.5,
        blurred = size > 7,
        drift = wind.side * 6,
        periodSeconds = 9 + seeded(i * 4 + 303) * 10,
        phaseSeconds = seeded(i * 4 + 304) * 20,
    )
}
