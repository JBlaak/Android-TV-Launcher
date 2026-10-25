package dev.jblaak.tvlauncher.apps

import android.content.Intent
import dev.jblaak.tvlauncher.home.LastWeather
import dev.jblaak.tvlauncher.home.WeatherSource

@Suppress("UNUSED_PARAMETER")
internal fun buildWeatherSource(intent: Intent, live: WeatherSource): WeatherSource = live

@Suppress("UNUSED_PARAMETER")
internal fun buildLastWeather(intent: Intent, saved: LastWeather): LastWeather = saved
