package dev.jblaak.tvlauncher.weather.internal

import dev.jblaak.tvlauncher.sky.SkyWeather
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
    weather = wmoWeather(current.weatherCode, current.temperature),
    temperature = current.temperature,
    wind = Wind(from = current.windDirection, speed = current.windSpeed, gusts = current.windGusts),
)

private fun wmoWeather(code: Int, temperature: Double): SkyWeather = when (code) {
    2, 3 -> SkyWeather.Clouds
    45, 48 -> SkyWeather.Fog
    in 51..67, in 80..82 -> SkyWeather.Rain
    in 71..77, 85, 86 -> SkyWeather.Snow
    in 95..99 -> SkyWeather.Storm
    else -> if (temperature >= 25) SkyWeather.Warm else SkyWeather.Clear
}
