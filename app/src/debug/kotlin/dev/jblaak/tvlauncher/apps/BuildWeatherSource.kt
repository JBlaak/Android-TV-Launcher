package dev.jblaak.tvlauncher.apps

import android.content.Intent
import dev.jblaak.tvlauncher.home.FetchedWeather
import dev.jblaak.tvlauncher.home.LastWeather
import dev.jblaak.tvlauncher.home.WeatherSource
import dev.jblaak.tvlauncher.sky.Weather
import dev.jblaak.tvlauncher.sky.Wind
import dev.jblaak.tvlauncher.weather.CurrentWeather
import dev.jblaak.tvlauncher.weather.WeatherFetch

/**
 * Debug builds can show any weather to check it by eye, for example
 * `adb shell am start -n dev.jblaak.tvlauncher/.HomeActivity --es weather rain --es temperature 9`. `--es weather
 * offline` acts as if the network is down. Without it, the live weather is used.
 */
internal fun buildWeatherSource(intent: Intent, live: WeatherSource): WeatherSource {
    val name = intent.getStringExtra("weather") ?: return live
    val temperature = intent.getStringExtra("temperature")?.toDoubleOrNull() ?: 9.0
    val weather = Weather.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    return object : WeatherSource {
        override suspend fun fetch(): WeatherFetch = weather?.let {
            WeatherFetch.Fetched(CurrentWeather(weather = it, temperature = temperature, wind = Wind(from = 225.0, speed = 20.0, gusts = 35.0)))
        } ?: WeatherFetch.Failed
    }
}

/**
 * Weather shown with `--es weather` is kept in memory only, so it never lingers in a later live run.
 */
internal fun buildLastWeather(intent: Intent, saved: LastWeather): LastWeather {
    if (intent.getStringExtra("weather") == null) return saved
    return object : LastWeather {
        private var weather: FetchedWeather? = null

        override fun get(): FetchedWeather? = weather

        override fun set(weather: FetchedWeather) {
            this.weather = weather
        }
    }
}
