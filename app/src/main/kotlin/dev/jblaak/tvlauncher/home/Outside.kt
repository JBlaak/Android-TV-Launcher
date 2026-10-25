package dev.jblaak.tvlauncher.home

import dev.jblaak.tvlauncher.R
import dev.jblaak.tvlauncher.sky.Location
import dev.jblaak.tvlauncher.sky.SkyScene
import dev.jblaak.tvlauncher.sky.SkyWeather
import dev.jblaak.tvlauncher.sky.Sun
import dev.jblaak.tvlauncher.sky.SunEvent
import dev.jblaak.tvlauncher.sky.Window
import dev.jblaak.tvlauncher.sky.nextSunEvent
import dev.jblaak.tvlauncher.sky.skyScene
import dev.jblaak.tvlauncher.weather.CurrentWeather
import java.time.ZonedDateTime

internal data class Outside(
    val sky: SkyScene,
    val sunTime: SunTime?,
    val weather: PillWeather,
)

internal fun outside(now: ZonedDateTime, window: Window, liveWeather: CurrentWeather?): Outside {
    val sky = if (liveWeather == null) {
        skyScene(now.toInstant(), window, SkyWeather.Clear)
    } else {
        skyScene(now.toInstant(), window, liveWeather.weather, liveWeather.wind)
    }
    return Outside(
        sky = sky,
        sunTime = nextSunTime(now, window.location),
        weather = PillWeather.of(liveWeather, sunUp = sky.sun != Sun.BelowHorizon),
    )
}

private fun nextSunTime(now: ZonedDateTime, location: Location): SunTime? = when (val event = nextSunEvent(now, location)) {
    is SunEvent.Sunrise -> SunTime(R.string.sunrise_time, event.at.toLocalTime())
    is SunEvent.Sunset -> SunTime(R.string.sunset_time, event.at.toLocalTime())
    null -> null
}
