package dev.jblaak.tvlauncher.home

import dev.jblaak.tvlauncher.sky.SkyWeather
import dev.jblaak.tvlauncher.sky.Wind
import dev.jblaak.tvlauncher.weather.CurrentWeather
import dev.jblaak.tvlauncher.weather.WeatherFetch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class HomeStateHolderFailureTest {

    @Test
    fun `when the sky or weather fails home shows no sky`() {
        /* When */
        val holder = createHolder(lastWeather = ThrowingLastWeather())

        /* Then */
        assertEquals(null, holder.state.value.sky)
    }

    @Test
    fun `when the sky or weather fails home shows no weather pill`() {
        /* When */
        val holder = createHolder(lastWeather = ThrowingLastWeather())

        /* Then */
        assertEquals(null to null, holder.state.value.let { it.weather to it.sunTime })
    }

    @Test
    fun `when the sky or weather fails home still shows the dock`() {
        /* When */
        val holder = createHolder(lastWeather = ThrowingLastWeather())

        /* Then */
        assertEquals(App.entries, holder.state.value.tiles.map { it.app })
    }

    @Test
    fun `when the sky or weather fails the clock still moves on`() = runTest {
        /* Given */
        val clock = FakeClock(at(17, 5, second = 30))
        val holder = createHolder(clock = clock, lastWeather = ThrowingLastWeather())
        backgroundScope.launch { holder.keepTime() }
        runCurrent()

        /* When */
        clock.time = at(17, 6)
        advanceTimeBy(30.seconds)
        runCurrent()

        /* Then */
        assertEquals(LocalTime.of(17, 6), holder.state.value.time)
    }

    @Test
    fun `a fetched weather brings the sky back after a failure`() = runTest {
        /* Given */
        val rain = CurrentWeather(weather = SkyWeather.Rain, temperature = 9.4, wind = Wind(from = 225.0, speed = 20.0, gusts = 35.0))
        val holder = createHolder(
            lastWeather = ThrowingLastWeather(),
            weatherSource = FakeWeatherSource(WeatherFetch.Fetched(rain)),
        )

        /* When */
        backgroundScope.launch { holder.keepWeather() }
        runCurrent()

        /* Then */
        assertNotNull(holder.state.value.sky)
    }

    private fun at(hour: Int, minute: Int, second: Int = 0): ZonedDateTime = ZonedDateTime.of(2026, 10, 25, hour, minute, second, 0, ZoneId.of("Europe/Amsterdam"))

    private fun createHolder(
        clock: Clock = FakeClock(at(17, 5)),
        weatherSource: WeatherSource = FakeWeatherSource(),
        lastWeather: LastWeather,
    ): HomeStateHolder = HomeStateHolder(
        config = HomeConfig.Enschede,
        installedApps = FakeInstalledApps(HomeConfig.Enschede.apps.flatMap { app -> app.targets.map { it.packageName } }.toSet()),
        lastOpenedApp = FakeLastOpenedApp(),
        clock = clock,
        weatherSource = weatherSource,
        lastWeather = lastWeather,
        listener = RecordingEvents(),
    )
}
