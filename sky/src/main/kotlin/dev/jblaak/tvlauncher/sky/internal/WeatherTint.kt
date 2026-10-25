package dev.jblaak.tvlauncher.sky.internal

import dev.jblaak.tvlauncher.sky.Rgb
import dev.jblaak.tvlauncher.sky.SkyWeather

/**
 * Each weather pulls the sky's base toward [base] and both glows toward [glow] by [mix], and scales the glows'
 * strength by [strength] (v1 mock-up: WEATHER).
 */
private data class WeatherTint(
    val base: Rgb,
    val glow: Rgb,
    val mix: Double,
    val strength: Double,
)

private fun SkyWeather.tint(): WeatherTint? = when (this) {
    SkyWeather.Clear -> null
    SkyWeather.Warm -> WeatherTint(Rgb(64, 36, 28), Rgb(255, 160, 70), 0.35, 1.2)
    SkyWeather.Clouds -> WeatherTint(Rgb(40, 44, 52), Rgb(150, 160, 175), 0.5, 0.7)
    SkyWeather.Rain -> WeatherTint(Rgb(22, 30, 42), Rgb(90, 115, 145), 0.6, 0.6)
    SkyWeather.Storm -> WeatherTint(Rgb(16, 16, 28), Rgb(95, 85, 150), 0.7, 0.7)
    SkyWeather.Snow -> WeatherTint(Rgb(50, 58, 76), Rgb(200, 215, 240), 0.55, 0.8)
    SkyWeather.Fog -> WeatherTint(Rgb(46, 50, 58), Rgb(180, 185, 195), 0.6, 0.6)
}

internal fun SkyKeyframe.tinted(weather: SkyWeather): SkyKeyframe {
    val tint = weather.tint() ?: return this
    return SkyKeyframe(
        base = base.blend(tint.base, tint.mix),
        warm = warm.blend(tint.glow, tint.mix),
        cool = cool.blend(tint.glow, tint.mix),
        strength = strength * tint.strength,
    )
}
