package dev.jblaak.tvlauncher.weather

import dev.jblaak.tvlauncher.sky.Location
import dev.jblaak.tvlauncher.weather.internal.OpenMeteoForecast
import dev.jblaak.tvlauncher.weather.internal.toCurrentWeather
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.IOException
import java.nio.channels.UnresolvedAddressException
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.time.Duration

class OpenMeteo(
    engine: HttpClientEngine,
    timeout: Duration,
) {

    private val client = HttpClient(engine) {
        expectSuccess = false
        install(HttpTimeout) { requestTimeoutMillis = timeout.inWholeMilliseconds }
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    suspend fun fetch(location: Location): WeatherFetch {
        val forecast = forecastOrNull(location) ?: return WeatherFetch.Failed
        return WeatherFetch.Fetched(forecast.toCurrentWeather())
    }

    private suspend fun forecastOrNull(location: Location): OpenMeteoForecast? = try {
        val response = client.get("https://api.open-meteo.com/v1/forecast") {
            parameter("latitude", location.latitude)
            parameter("longitude", location.longitude)
            parameter("current", "temperature_2m,weather_code,wind_speed_10m,wind_direction_10m,wind_gusts_10m")
        }
        if (response.status.isSuccess()) response.body<OpenMeteoForecast>() else null
    } catch (noConnection: IOException) {
        logger.log(Level.WARNING, "Open-Meteo can't be reached", noConnection)
        null
    } catch (unknownHost: UnresolvedAddressException) {
        logger.log(Level.WARNING, "Open-Meteo's address can't be resolved", unknownHost)
        null
    } catch (unreadable: ContentConvertException) {
        logger.log(Level.WARNING, "Open-Meteo's forecast can't be read", unreadable)
        null
    }
}

private val logger = Logger.getLogger("OpenMeteo")
