package dev.jblaak.tvlauncher.home

import dev.jblaak.tvlauncher.weather.CurrentWeather
import dev.jblaak.tvlauncher.weather.WeatherFetch
import java.time.Instant

/**
 * The live weather where the TV hangs.
 */
internal interface WeatherSource {
    suspend fun fetch(): WeatherFetch
}

/**
 * The weather last fetched, kept across restarts.
 */
internal interface LastWeather {
    fun get(): FetchedWeather?

    fun set(weather: FetchedWeather)
}

internal data class FetchedWeather(
    val current: CurrentWeather,
    val at: Instant,
)
