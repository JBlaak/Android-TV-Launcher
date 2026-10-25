package dev.jblaak.tvlauncher.weather.internal

import dev.jblaak.tvlauncher.sky.Weather
import dev.jblaak.tvlauncher.sky.Wind
import dev.jblaak.tvlauncher.weather.CurrentWeather
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OpenMeteoForecast(
    val current: Current,
) {
    @Serializable
    data class Current(
        @SerialName("temperature_2m") val temperature: Double,
        @SerialName("weather_code") val weatherCode: Int,
        @SerialName("wind_speed_10m") val windSpeed: Double,
        @SerialName("wind_direction_10m") val windDirection: Double,
        @SerialName("wind_gusts_10m") val windGusts: Double,
    )
}

internal fun OpenMeteoForecast.toCurrentWeather(): CurrentWeather = CurrentWeather(
    weather = weather(current.weatherCode, current.temperature),
    temperature = current.temperature,
    wind = Wind(from = current.windDirection, speed = current.windSpeed, gusts = current.windGusts),
)

/**
 * WMO weather interpretation codes as Open-Meteo documents them. A clear sky from 25 °C is [Weather.Warm].
 */
private fun weather(code: Int, temperature: Double): Weather = when (code) {
    2, 3 -> Weather.Cloudy
    45, 48 -> Weather.Fog
    in 51..67, in 80..82 -> Weather.Rain
    in 71..77, 85, 86 -> Weather.Snow
    in 95..99 -> Weather.Storm
    else -> if (temperature >= 25) Weather.Warm else Weather.Clear
}
