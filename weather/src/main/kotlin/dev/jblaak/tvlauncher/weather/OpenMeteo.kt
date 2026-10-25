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
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlin.time.Duration

/**
 * The current weather from Open-Meteo (open-meteo.com), which needs no API key. Any failure, from no network to a
 * response it can't read, comes back as [WeatherFetch.Failed].
 */
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

    @Suppress("TooGenericExceptionCaught")
    private suspend fun forecastOrNull(location: Location): OpenMeteoForecast? = try {
        val response = client.get("https://api.open-meteo.com/v1/forecast") {
            parameter("latitude", location.latitude)
            parameter("longitude", location.longitude)
            parameter("current", "temperature_2m,weather_code,wind_speed_10m,wind_direction_10m,wind_gusts_10m")
        }
        if (response.status.isSuccess()) response.body<OpenMeteoForecast>() else null
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        null
    }
}
