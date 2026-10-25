package dev.jblaak.tvlauncher.home

internal class FakeLastWeather(
    var weather: FetchedWeather? = null,
) : LastWeather {

    override fun get(): FetchedWeather? = weather

    override fun set(weather: FetchedWeather) {
        this.weather = weather
    }
}
