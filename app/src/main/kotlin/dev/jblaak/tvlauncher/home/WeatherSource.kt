package dev.jblaak.tvlauncher.home

import dev.jblaak.tvlauncher.weather.CurrentWeather
import dev.jblaak.tvlauncher.weather.WeatherFetch
import java.time.Instant

internal interface WeatherSource {
    suspend fun fetch(): WeatherFetch
}

internal interface LastWeather {
    fun get(): FetchedWeather?

    fun set(weather: FetchedWeather)
}

internal data class FetchedWeather(
    val current: CurrentWeather,
    val at: Instant,
)
