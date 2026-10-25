package dev.jblaak.tvlauncher.home

import dev.jblaak.tvlauncher.weather.WeatherFetch

internal class FakeWeatherSource(
    var next: WeatherFetch = WeatherFetch.Failed,
) : WeatherSource {

    var fetches = 0
        private set

    override suspend fun fetch(): WeatherFetch {
        fetches++
        return next
    }
}
