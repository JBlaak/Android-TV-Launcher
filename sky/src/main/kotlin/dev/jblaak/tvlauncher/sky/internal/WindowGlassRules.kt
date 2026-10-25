package dev.jblaak.tvlauncher.sky.internal

import dev.jblaak.tvlauncher.sky.Bead
import dev.jblaak.tvlauncher.sky.ScreenPoint
import dev.jblaak.tvlauncher.sky.SkyWeather
import dev.jblaak.tvlauncher.sky.Slide
import dev.jblaak.tvlauncher.sky.WindowGlass

/**
 * Rain and storms leave beads on the glass, snow frosts it (canvas copy, DockNight whimsyFx). The mock-up's 34 beads
 * become 30, with the mock-up's seeds; every fourth bead slides.
 */
internal fun windowGlass(weather: SkyWeather): WindowGlass = when (weather) {
    SkyWeather.Rain, SkyWeather.Storm -> WindowGlass(beads = beads(), frost = false)
    SkyWeather.Snow -> WindowGlass(beads = emptyList(), frost = true)
    SkyWeather.Clear, SkyWeather.Warm, SkyWeather.Clouds, SkyWeather.Fog -> WindowGlass.Clean
}

private fun beads(): List<Bead> = List(30) { i ->
    val size = 5 + seeded(i * 4 + 700) * 13
    Bead(
        topLeft = ScreenPoint(seeded(i * 4 + 701) * 1280, seeded(i * 4 + 702) * 0.92 * 720),
        width = size,
        height = size * 1.12,
        slide = if (i % 4 == 0) Slide(periodSeconds = 9 + seeded(i + 710) * 8, phaseSeconds = seeded(i + 711) * 12) else null,
    )
}
