package dev.jblaak.tvlauncher.home

import dev.jblaak.tvlauncher.R
import dev.jblaak.tvlauncher.sky.SkyWeather
import dev.jblaak.tvlauncher.sky.Wind
import dev.jblaak.tvlauncher.sky.skyScene
import dev.jblaak.tvlauncher.weather.CurrentWeather
import dev.jblaak.tvlauncher.weather.WeatherFetch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class HomeStateHolderWeatherTest {

    @Test
    fun `without any weather the pill shows a clear night without a temperature`() {
        /* When */
        val holder = createHolder(clock = FakeClock(at(21, 15)))

        /* Then */
        assertEquals(PillWeather(R.drawable.ic_weather_moon, R.string.clear, temperature = null, wind = null), holder.state.value.weather)
    }

    @Test
    fun `without any weather the sky is clear`() {
        /* When */
        val holder = createHolder(clock = FakeClock(at(17, 5)))

        /* Then */
        assertEquals(skyScene(at(17, 5).toInstant(), HomeConfig.Enschede.window, SkyWeather.Clear), holder.state.value.sky)
    }

    @Test
    fun `coming home without any weather fetches it`() = runTest {
        /* Given */
        val source = FakeWeatherSource()
        val holder = createHolder(weatherSource = source)

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(1, source.fetches)
    }

    @Test
    fun `fetched rain shows in the pill`() = runTest {
        /* Given */
        val holder = createHolder(weatherSource = FakeWeatherSource(fetched(rain)))

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(PillWeather(R.drawable.ic_weather_rain, R.string.rain, temperature = 9, wind = null), holder.state.value.weather)
    }

    @Test
    fun `the pill rounds the temperature`() = runTest {
        /* Given */
        val holder = createHolder(weatherSource = FakeWeatherSource(fetched(rain.copy(temperature = 9.6))))

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(10, holder.state.value.weather?.temperature)
    }

    @Test
    fun `the pill shows temperatures below zero`() = runTest {
        /* Given */
        val holder = createHolder(weatherSource = FakeWeatherSource(fetched(rain.copy(weather = SkyWeather.Snow, temperature = -2.4))))

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(-2, holder.state.value.weather?.temperature)
    }

    @Test
    fun `fetched rain tints the sky`() = runTest {
        /* Given */
        val clock = FakeClock(at(17, 5))
        val holder = createHolder(clock = clock, weatherSource = FakeWeatherSource(fetched(rain)))

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(skyScene(at(17, 5).toInstant(), HomeConfig.Enschede.window, SkyWeather.Rain, rain.wind), holder.state.value.sky)
    }

    @Test
    fun `the live wind blows through the sky`() = runTest {
        /* Given */
        val storm = rain.copy(weather = SkyWeather.Storm, wind = Wind(from = 200.0, speed = 60.0, gusts = 90.0))
        val holder = createHolder(clock = FakeClock(at(17, 5)), weatherSource = FakeWeatherSource(fetched(storm)))

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(skyScene(at(17, 5).toInstant(), HomeConfig.Enschede.window, SkyWeather.Storm, storm.wind), holder.state.value.sky)
    }

    @Test
    fun `a wind of 50 km per hour shows in the pill`() = runTest {
        /* Given */
        val storm = rain.copy(weather = SkyWeather.Storm, wind = Wind(from = 225.0, speed = 50.0, gusts = 80.0))
        val holder = createHolder(weatherSource = FakeWeatherSource(fetched(storm)))

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(PillWind(R.string.compass_south_west, 50), holder.state.value.weather?.wind)
    }

    @Test
    fun `a wind below 50 km per hour leaves the pill to the sun time`() = runTest {
        /* Given */
        val holder = createHolder(weatherSource = FakeWeatherSource(fetched(rain.copy(wind = Wind(from = 225.0, speed = 49.4, gusts = 80.0)))))

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(null, holder.state.value.weather?.wind)
    }

    @Test
    fun `the pill rounds the wind speed`() = runTest {
        /* Given */
        val holder = createHolder(weatherSource = FakeWeatherSource(fetched(rain.copy(wind = Wind(from = 225.0, speed = 74.6, gusts = 90.0)))))

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(75, holder.state.value.weather?.wind?.speed)
    }

    @Test
    fun `the pill names the nearest of eight compass points the wind blows from`() = runTest {
        /* Given */
        val holder = createHolder(weatherSource = FakeWeatherSource(fetched(rain.copy(wind = Wind(from = 200.0, speed = 60.0, gusts = 90.0)))))

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(R.string.compass_south, holder.state.value.weather?.wind?.from)
    }

    @Test
    fun `a wind from just west of north comes from the north`() = runTest {
        /* Given */
        val holder = createHolder(weatherSource = FakeWeatherSource(fetched(rain.copy(wind = Wind(from = 350.0, speed = 60.0, gusts = 90.0)))))

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(R.string.compass_north, holder.state.value.weather?.wind?.from)
    }

    @Test
    fun `the fetched weather is saved for next time`() = runTest {
        /* Given */
        val lastWeather = FakeLastWeather()
        val holder = createHolder(clock = FakeClock(at(17, 5)), weatherSource = FakeWeatherSource(fetched(rain)), lastWeather = lastWeather)

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(FetchedWeather(rain, at(17, 5).toInstant()), lastWeather.weather)
    }

    @Test
    fun `weather fetched less than 15 minutes ago is not fetched again`() = runTest {
        /* Given */
        val source = FakeWeatherSource()
        val lastWeather = FakeLastWeather(FetchedWeather(rain, at(17, 0).toInstant()))
        val holder = createHolder(clock = FakeClock(at(17, 14)), weatherSource = source, lastWeather = lastWeather)

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(0, source.fetches)
    }

    @Test
    fun `weather fetched more than 15 minutes ago is fetched again`() = runTest {
        /* Given */
        val source = FakeWeatherSource()
        val lastWeather = FakeLastWeather(FetchedWeather(rain, at(17, 0).toInstant()))
        val holder = createHolder(clock = FakeClock(at(17, 16)), weatherSource = source, lastWeather = lastWeather)

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(1, source.fetches)
    }

    @Test
    fun `while home stays open the weather is fetched again after 15 minutes`() = runTest {
        /* Given */
        val clock = FakeClock(at(17, 0))
        val source = FakeWeatherSource(fetched(rain))
        val holder = createHolder(clock = clock, weatherSource = source)
        keepWeather(holder)

        /* When */
        clock.time = at(17, 16)
        advanceTimeBy(16.minutes)
        runCurrent()

        /* Then */
        assertEquals(2, source.fetches)
    }

    @Test
    fun `while home stays open it doesn't fetch the weather more often`() = runTest {
        /* Given */
        val clock = FakeClock(at(17, 0))
        val source = FakeWeatherSource(fetched(rain))
        val holder = createHolder(clock = clock, weatherSource = source)
        keepWeather(holder)

        /* When */
        clock.time = at(17, 10)
        advanceTimeBy(10.minutes)
        runCurrent()

        /* Then */
        assertEquals(1, source.fetches)
    }

    @Test
    fun `saved weather from the last 3 hours shows straight away`() {
        /* Given */
        val lastWeather = FakeLastWeather(FetchedWeather(rain, at(15, 0).toInstant()))

        /* When */
        val holder = createHolder(clock = FakeClock(at(17, 59)), lastWeather = lastWeather)

        /* Then */
        assertEquals(PillWeather(R.drawable.ic_weather_rain, R.string.rain, temperature = 9, wind = null), holder.state.value.weather)
    }

    @Test
    fun `saved weather older than 3 hours shows a clear sky without a temperature`() {
        /* Given */
        val lastWeather = FakeLastWeather(FetchedWeather(rain, at(18, 0).toInstant()))

        /* When */
        val holder = createHolder(clock = FakeClock(at(21, 1)), lastWeather = lastWeather)

        /* Then */
        assertEquals(PillWeather(R.drawable.ic_weather_moon, R.string.clear, temperature = null, wind = null), holder.state.value.weather)
    }

    @Test
    fun `the weather turns clear once it is 3 hours old`() = runTest {
        /* Given */
        val clock = FakeClock(at(17, 59, second = 30))
        val lastWeather = FakeLastWeather(FetchedWeather(rain, at(15, 0).toInstant()))
        val holder = createHolder(clock = clock, lastWeather = lastWeather)
        backgroundScope.launch { holder.keepTime() }
        runCurrent()

        /* When */
        clock.time = at(18, 1)
        advanceTimeBy(30_000)
        runCurrent()

        /* Then */
        assertEquals(null, holder.state.value.weather?.temperature)
    }

    @Test
    fun `a failed fetch keeps the last weather`() = runTest {
        /* Given */
        val lastWeather = FakeLastWeather(FetchedWeather(rain, at(17, 0).toInstant()))
        val holder = createHolder(clock = FakeClock(at(17, 30)), weatherSource = FakeWeatherSource(WeatherFetch.Failed), lastWeather = lastWeather)

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(R.string.rain, holder.state.value.weather?.condition)
    }

    @Test
    fun `a clear day shows the sun and says sunny`() = runTest {
        /* Given */
        val holder = createHolder(clock = FakeClock(at(12, 0)), weatherSource = FakeWeatherSource(fetched(rain.copy(weather = SkyWeather.Clear))))

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(R.drawable.ic_weather_sun to R.string.sunny, holder.state.value.weather?.let { it.icon to it.condition })
    }

    @Test
    fun `a warm day says sunny too`() = runTest {
        /* Given */
        val holder = createHolder(clock = FakeClock(at(12, 0)), weatherSource = FakeWeatherSource(fetched(rain.copy(weather = SkyWeather.Warm))))

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(R.drawable.ic_weather_sun to R.string.sunny, holder.state.value.weather?.let { it.icon to it.condition })
    }

    @Test
    fun `a warm night shows the moon and says warm`() = runTest {
        /* Given */
        val holder = createHolder(clock = FakeClock(at(23, 0)), weatherSource = FakeWeatherSource(fetched(rain.copy(weather = SkyWeather.Warm))))

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(R.drawable.ic_weather_moon to R.string.warm, holder.state.value.weather?.let { it.icon to it.condition })
    }

    @Test
    fun `cloudy weather shows clouds`() = runTest {
        /* Given */
        val holder = createHolder(weatherSource = FakeWeatherSource(fetched(rain.copy(weather = SkyWeather.Clouds))))

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(R.drawable.ic_weather_cloudy to R.string.cloudy, holder.state.value.weather?.let { it.icon to it.condition })
    }

    @Test
    fun `a storm shows lightning`() = runTest {
        /* Given */
        val holder = createHolder(weatherSource = FakeWeatherSource(fetched(rain.copy(weather = SkyWeather.Storm))))

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(R.drawable.ic_weather_storm to R.string.storm, holder.state.value.weather?.let { it.icon to it.condition })
    }

    @Test
    fun `snow shows snowflakes`() = runTest {
        /* Given */
        val holder = createHolder(weatherSource = FakeWeatherSource(fetched(rain.copy(weather = SkyWeather.Snow))))

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(R.drawable.ic_weather_snow to R.string.snow, holder.state.value.weather?.let { it.icon to it.condition })
    }

    @Test
    fun `fog shows fog`() = runTest {
        /* Given */
        val holder = createHolder(weatherSource = FakeWeatherSource(fetched(rain.copy(weather = SkyWeather.Fog))))

        /* When */
        keepWeather(holder)

        /* Then */
        assertEquals(R.drawable.ic_weather_fog to R.string.fog, holder.state.value.weather?.let { it.icon to it.condition })
    }

    private fun TestScope.keepWeather(holder: HomeStateHolder) {
        backgroundScope.launch { holder.keepWeather() }
        runCurrent()
    }

    private fun fetched(current: CurrentWeather): WeatherFetch = WeatherFetch.Fetched(current)

    private fun at(hour: Int, minute: Int, second: Int = 0): ZonedDateTime = ZonedDateTime.of(2026, 10, 25, hour, minute, second, 0, ZoneId.of("Europe/Amsterdam"))

    private fun createHolder(
        clock: Clock = FakeClock(at(21, 15)),
        weatherSource: WeatherSource = FakeWeatherSource(),
        lastWeather: LastWeather = FakeLastWeather(),
    ): HomeStateHolder = HomeStateHolder(
        config = HomeConfig.Enschede,
        installedApps = FakeInstalledApps(emptySet()),
        lastOpenedApp = FakeLastOpenedApp(),
        clock = clock,
        weatherSource = weatherSource,
        lastWeather = lastWeather,
        listener = RecordingEvents(),
    )

    private companion object {
        val rain = CurrentWeather(weather = SkyWeather.Rain, temperature = 9.4, wind = Wind(from = 225.0, speed = 20.0, gusts = 35.0))
    }
}
