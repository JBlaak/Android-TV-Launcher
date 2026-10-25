package dev.jblaak.tvlauncher.weather

import android.content.SharedPreferences
import androidx.core.content.edit
import dev.jblaak.tvlauncher.home.FetchedWeather
import dev.jblaak.tvlauncher.home.LastWeather
import dev.jblaak.tvlauncher.sky.Weather
import dev.jblaak.tvlauncher.sky.Wind
import java.time.Instant

internal class SharedPreferencesLastWeather(
    private val preferences: SharedPreferences,
) : LastWeather {

    override fun get(): FetchedWeather? {
        val weather = Weather.entries.firstOrNull { it.name == preferences.getString("weather", null) } ?: return null
        return FetchedWeather(
            current = CurrentWeather(
                weather = weather,
                temperature = preferences.getDouble("temperature"),
                wind = Wind(from = preferences.getDouble("wind_from"), speed = preferences.getDouble("wind_speed"), gusts = preferences.getDouble("wind_gusts")),
            ),
            at = Instant.ofEpochMilli(preferences.getLong("fetched_at", 0)),
        )
    }

    override fun set(weather: FetchedWeather) {
        preferences.edit {
            putString("weather", weather.current.weather.name)
            putDouble("temperature", weather.current.temperature)
            putDouble("wind_from", weather.current.wind.from)
            putDouble("wind_speed", weather.current.wind.speed)
            putDouble("wind_gusts", weather.current.wind.gusts)
            putLong("fetched_at", weather.at.toEpochMilli())
        }
    }

    private fun SharedPreferences.getDouble(key: String): Double = Double.fromBits(getLong(key, 0))

    private fun SharedPreferences.Editor.putDouble(key: String, value: Double) {
        putLong(key, value.toRawBits())
    }
}
