package dev.jblaak.tvlauncher.home

internal class ThrowingLastWeather : LastWeather {

    override fun get(): FetchedWeather? = throw IllegalStateException("The saved weather can't be read")

    override fun set(weather: FetchedWeather) = Unit
}
