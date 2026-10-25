package dev.jblaak.tvlauncher.weather

import dev.jblaak.tvlauncher.sky.Weather
import dev.jblaak.tvlauncher.sky.Wind

/**
 * The weather outside right now, with the [temperature] in degrees Celsius.
 */
data class CurrentWeather(
    val weather: Weather,
    val temperature: Double,
    val wind: Wind,
)

sealed interface WeatherFetch {
    data class Fetched(val current: CurrentWeather) : WeatherFetch

    data object Failed : WeatherFetch
}
