package dev.jblaak.tvlauncher.weather

import dev.jblaak.tvlauncher.home.HomeConfig
import dev.jblaak.tvlauncher.home.WeatherSource
import io.ktor.client.engine.cio.CIO
import kotlin.time.Duration.Companion.seconds

/**
 * The live weather in Enschede from Open-Meteo, for Home and the screensaver.
 */
internal fun openMeteoWeatherSource(): WeatherSource {
    val openMeteo = OpenMeteo(engine = CIO.create(), timeout = 10.seconds)
    return object : WeatherSource {
        override suspend fun fetch(): WeatherFetch = openMeteo.fetch(HomeConfig.Enschede.window.location)
    }
}
