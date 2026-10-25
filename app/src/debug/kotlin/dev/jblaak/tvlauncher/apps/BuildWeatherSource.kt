package dev.jblaak.tvlauncher.apps

import android.content.Intent
import dev.jblaak.tvlauncher.home.FetchedWeather
import dev.jblaak.tvlauncher.home.LastWeather
import dev.jblaak.tvlauncher.home.WeatherSource
import dev.jblaak.tvlauncher.sky.SkyWeather
import dev.jblaak.tvlauncher.sky.Wind
import dev.jblaak.tvlauncher.sky.typicalWind
import dev.jblaak.tvlauncher.weather.CurrentWeather
import dev.jblaak.tvlauncher.weather.WeatherFetch

/**
 * Debug builds can show any weather to check it by eye, for example
 * `adb shell am start -n dev.jblaak.tvlauncher/.HomeActivity --es weather rain --es temperature 9`. `--es wind_from 180
 * --es wind_speed 60` sets the wind, which is otherwise the typical wind for that weather. `--es weather offline`
 * acts as if the network is down. Without it, the live weather is used.
 */
internal fun buildWeatherSource(intent: Intent, live: WeatherSource): WeatherSource {
    val name = intent.getStringExtra("weather") ?: return live
    val temperature = intent.getStringExtra("temperature")?.toDoubleOrNull() ?: 9.0
    val weather = SkyWeather.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    return object : WeatherSource {
        override suspend fun fetch(): WeatherFetch = weather?.let {
            WeatherFetch.Fetched(CurrentWeather(weather = it, temperature = temperature, wind = wind(intent, it)))
        } ?: WeatherFetch.Failed
    }
}

private fun wind(intent: Intent, weather: SkyWeather): Wind {
    val typical = typicalWind(weather)
    val speed = intent.getStringExtra("wind_speed")?.toDoubleOrNull() ?: typical.speed
    return Wind(
        from = intent.getStringExtra("wind_from")?.toDoubleOrNull() ?: typical.from,
        speed = speed,
        gusts = speed * 1.5,
    )
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
