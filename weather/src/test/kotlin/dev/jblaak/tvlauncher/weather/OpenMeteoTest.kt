package dev.jblaak.tvlauncher.weather

import dev.jblaak.tvlauncher.sky.Location
import dev.jblaak.tvlauncher.sky.SkyWeather
import dev.jblaak.tvlauncher.sky.Wind
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class OpenMeteoTest {

    @Test
    fun `it asks for the current weather at the location`() = runTest {
        /* Given */
        val requests = mutableListOf<Url>()
        val openMeteo = createOpenMeteo { request ->
            requests += request.url
            respondJson(canned("current_rain.json"))
        }

        /* When */
        openMeteo.fetch(enschede)

        /* Then */
        val url = requests.single()
        assertEquals(
            listOf("api.open-meteo.com", "/v1/forecast", "52.2215", "6.8937", "temperature_2m,weather_code,wind_speed_10m,wind_direction_10m,wind_gusts_10m"),
            listOf(url.host, url.encodedPath, url.parameters["latitude"], url.parameters["longitude"], url.parameters["current"]),
        )
    }

    @Test
    fun `it reads the temperature in degrees celsius`() = runTest {
        /* Given */
        val openMeteo = createOpenMeteo { respondJson(canned("current_rain.json")) }

        /* When */
        val fetch = openMeteo.fetch(enschede)

        /* Then */
        assertEquals(9.4, assertIs<WeatherFetch.Fetched>(fetch).current.temperature)
    }

    @Test
    fun `it reads the wind direction, speed and gusts in km per hour`() = runTest {
        /* Given */
        val openMeteo = createOpenMeteo { respondJson(canned("current_rain.json")) }

        /* When */
        val fetch = openMeteo.fetch(enschede)

        /* Then */
        assertEquals(Wind(from = 230.0, speed = 21.2, gusts = 48.6), assertIs<WeatherFetch.Fetched>(fetch).current.wind)
    }

    @Test
    fun `a clear sky and a mainly clear sky are clear`() = runTest {
        /* When */
        val weather = weatherFor(0, 1)

        /* Then */
        assertEquals(listOf(SkyWeather.Clear, SkyWeather.Clear), weather)
    }

    @Test
    fun `a clear sky from 25 degrees is warm`() = runTest {
        /* When */
        val weather = weatherFor(0, 1, temperature = 25.0)

        /* Then */
        assertEquals(listOf(SkyWeather.Warm, SkyWeather.Warm), weather)
    }

    @Test
    fun `a clear sky just below 25 degrees is still clear`() = runTest {
        /* When */
        val weather = weatherFor(0, temperature = 24.9)

        /* Then */
        assertEquals(listOf(SkyWeather.Clear), weather)
    }

    @Test
    fun `partly cloudy and overcast are cloudy`() = runTest {
        /* When */
        val weather = weatherFor(2, 3)

        /* Then */
        assertEquals(listOf(SkyWeather.Clouds, SkyWeather.Clouds), weather)
    }

    @Test
    fun `cloudy stays cloudy when it's hot`() = runTest {
        /* When */
        val weather = weatherFor(3, temperature = 30.0)

        /* Then */
        assertEquals(listOf(SkyWeather.Clouds), weather)
    }

    @Test
    fun `fog and rime fog are fog`() = runTest {
        /* When */
        val weather = weatherFor(45, 48)

        /* Then */
        assertEquals(listOf(SkyWeather.Fog, SkyWeather.Fog), weather)
    }

    @Test
    fun `drizzle is rain`() = runTest {
        /* When */
        val weather = weatherFor(51, 53, 55, 56, 57)

        /* Then */
        assertEquals(List(5) { SkyWeather.Rain }, weather)
    }

    @Test
    fun `rain and freezing rain are rain`() = runTest {
        /* When */
        val weather = weatherFor(61, 63, 65, 66, 67)

        /* Then */
        assertEquals(List(5) { SkyWeather.Rain }, weather)
    }

    @Test
    fun `rain showers are rain`() = runTest {
        /* When */
        val weather = weatherFor(80, 81, 82)

        /* Then */
        assertEquals(List(3) { SkyWeather.Rain }, weather)
    }

    @Test
    fun `snowfall and snow grains are snow`() = runTest {
        /* When */
        val weather = weatherFor(71, 73, 75, 77)

        /* Then */
        assertEquals(List(4) { SkyWeather.Snow }, weather)
    }

    @Test
    fun `snow showers are snow`() = runTest {
        /* When */
        val weather = weatherFor(85, 86)

        /* Then */
        assertEquals(List(2) { SkyWeather.Snow }, weather)
    }

    @Test
    fun `thunderstorms, with or without hail, are storms`() = runTest {
        /* When */
        val weather = weatherFor(95, 96, 99)

        /* Then */
        assertEquals(List(3) { SkyWeather.Storm }, weather)
    }

    @Test
    fun `an unknown weather code is treated as clear`() = runTest {
        /* When */
        val weather = weatherFor(42)

        /* Then */
        assertEquals(listOf(SkyWeather.Clear), weather)
    }

    @Test
    fun `an http error fails the fetch`() = runTest {
        /* Given */
        val openMeteo = createOpenMeteo { respondError(HttpStatusCode.InternalServerError) }

        /* When */
        val fetch = openMeteo.fetch(enschede)

        /* Then */
        assertEquals(WeatherFetch.Failed, fetch)
    }

    @Test
    fun `an error from open-meteo fails the fetch`() = runTest {
        /* Given */
        val openMeteo = createOpenMeteo { respondJson(canned("error_bad_request.json"), HttpStatusCode.BadRequest) }

        /* When */
        val fetch = openMeteo.fetch(enschede)

        /* Then */
        assertEquals(WeatherFetch.Failed, fetch)
    }

    @Test
    fun `malformed json fails the fetch`() = runTest {
        /* Given */
        val openMeteo = createOpenMeteo { respondJson("{\"current\": {\"temperature_2m\": ") }

        /* When */
        val fetch = openMeteo.fetch(enschede)

        /* Then */
        assertEquals(WeatherFetch.Failed, fetch)
    }

    @Test
    fun `a response without the current weather fails the fetch`() = runTest {
        /* Given */
        val openMeteo = createOpenMeteo { respondJson("{\"latitude\": 52.22, \"longitude\": 6.9}") }

        /* When */
        val fetch = openMeteo.fetch(enschede)

        /* Then */
        assertEquals(WeatherFetch.Failed, fetch)
    }

    @Test
    fun `a response slower than the timeout fails the fetch`() = runTest {
        /* Given */
        val openMeteo = createOpenMeteo(timeout = 100.milliseconds) {
            delay(5.seconds)
            respondJson(canned("current_rain.json"))
        }

        /* When */
        val fetch = openMeteo.fetch(enschede)

        /* Then */
        assertEquals(WeatherFetch.Failed, fetch)
    }

    @Test
    fun `no network fails the fetch`() = runTest {
        /* Given */
        val openMeteo = createOpenMeteo { throw UnknownHostException("api.open-meteo.com") }

        /* When */
        val fetch = openMeteo.fetch(enschede)

        /* Then */
        assertEquals(WeatherFetch.Failed, fetch)
    }

    @Test
    fun `an address that can't be resolved fails the fetch`() = runTest {
        /* Given */
        val openMeteo = createOpenMeteo { throw UnresolvedAddressException() }

        /* When */
        val fetch = openMeteo.fetch(enschede)

        /* Then */
        assertEquals(WeatherFetch.Failed, fetch)
    }

    private suspend fun weatherFor(vararg codes: Int, temperature: Double = 12.0): List<SkyWeather> = codes.map { code ->
        val json = canned("current_rain.json")
            .replace("\"weather_code\":61", "\"weather_code\":$code")
            .replace("\"temperature_2m\":9.4", "\"temperature_2m\":$temperature")
        val fetch = createOpenMeteo { respondJson(json) }.fetch(enschede)
        assertIs<WeatherFetch.Fetched>(fetch).current.weather
    }

    private fun MockRequestHandleScope.respondJson(
        json: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ) = respond(json, status, headersOf(HttpHeaders.ContentType, "application/json"))

    private fun canned(name: String): String = checkNotNull(javaClass.classLoader.getResource(name)).readText()

    private fun createOpenMeteo(timeout: Duration = 10.seconds, handler: MockRequestHandler): OpenMeteo = OpenMeteo(engine = MockEngine(handler), timeout = timeout)

    private companion object {
        val enschede = Location(latitude = 52.2215, longitude = 6.8937)
    }
}
