package dev.jblaak.tvlauncher.home

import dev.jblaak.tvlauncher.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class HomeMomentsTest {

    @Test
    fun `on the day the clocks go back the greeting says so`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2026, 10, 25, 17, 5)))

        /* Then */
        assertEquals(Greeting.Plain(R.string.clocks_went_back), holder.state.value.greeting)
    }

    @Test
    fun `just after the clocks go back the greeting says so`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2026, 10, 25, 3, 30)))

        /* Then */
        assertEquals(Greeting.Plain(R.string.clocks_went_back), holder.state.value.greeting)
    }

    @Test
    fun `on the day the clocks go forward the greeting says so`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2026, 3, 29, 9, 0)))

        /* Then */
        assertEquals(Greeting.Plain(R.string.clocks_went_forward), holder.state.value.greeting)
    }

    @Test
    fun `the day before the clocks go back the greeting follows the time of day`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2026, 10, 24, 17, 5)))

        /* Then */
        assertEquals(Greeting.Plain(R.string.good_afternoon), holder.state.value.greeting)
    }

    @Test
    fun `a day after the clocks went back the greeting follows the time of day again`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2026, 10, 26, 12, 0)))

        /* Then */
        assertEquals(Greeting.Plain(R.string.good_afternoon), holder.state.value.greeting)
    }

    @Test
    fun `the clock change greeting follows a time zone that changes its clocks on another day`() {
        /* When */
        val holder = createHolder(FakeClock(ZonedDateTime.of(2026, 11, 1, 12, 0, 0, 0, ZoneId.of("America/New_York"))))

        /* Then */
        assertEquals(Greeting.Plain(R.string.clocks_went_back), holder.state.value.greeting)
    }

    @Test
    fun `the clock change brings no fireworks or bunting`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2026, 10, 25, 17, 5)))

        /* Then */
        assertEquals(null, holder.state.value.celebration)
    }

    @Test
    fun `from 23 00 on new year's eve the greeting looks ahead to the new year`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2026, 12, 31, 23, 0)))

        /* Then */
        assertEquals(Greeting.WithYear(R.string.almost_year, 2027), holder.state.value.greeting)
    }

    @Test
    fun `just before 23 00 on new year's eve the greeting follows the time of day`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2026, 12, 31, 22, 59)))

        /* Then */
        assertEquals(Greeting.Plain(R.string.good_evening), holder.state.value.greeting)
    }

    @Test
    fun `after midnight on new year's day the greeting wishes a happy new year`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2027, 1, 1, 0, 0)))

        /* Then */
        assertEquals(Greeting.WithYear(R.string.happy_year, 2027), holder.state.value.greeting)
    }

    @Test
    fun `just before 02 00 on new year's day the greeting still wishes a happy new year`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2027, 1, 1, 1, 59)))

        /* Then */
        assertEquals(Greeting.WithYear(R.string.happy_year, 2027), holder.state.value.greeting)
    }

    @Test
    fun `from 02 00 on new year's day the greeting follows the time of day`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2027, 1, 1, 2, 0)))

        /* Then */
        assertEquals(Greeting.Plain(R.string.good_night), holder.state.value.greeting)
    }

    @Test
    fun `fireworks go off around midnight on new year's eve`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2026, 12, 31, 23, 30)))

        /* Then */
        assertEquals(Celebration.Fireworks, holder.state.value.celebration)
    }

    @Test
    fun `fireworks go on after midnight`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2027, 1, 1, 1, 0)))

        /* Then */
        assertEquals(Celebration.Fireworks, holder.state.value.celebration)
    }

    @Test
    fun `fireworks stop at 02 00 on new year's day`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2027, 1, 1, 2, 0)))

        /* Then */
        assertEquals(null, holder.state.value.celebration)
    }

    @Test
    fun `fireworks start when the clock reaches 23 00 on new year's eve`() = runTest {
        /* Given */
        val clock = FakeClock(amsterdam(2026, 12, 31, 22, 59, second = 30))
        val holder = createHolder(clock)
        backgroundScope.launch { holder.keepTime() }
        runCurrent()

        /* When */
        clock.time = amsterdam(2026, 12, 31, 23, 0)
        advanceTimeBy(30.seconds)
        runCurrent()

        /* Then */
        assertEquals(Celebration.Fireworks, holder.state.value.celebration)
    }

    @Test
    fun `the greeting turns to a happy new year at midnight`() = runTest {
        /* Given */
        val clock = FakeClock(amsterdam(2026, 12, 31, 23, 59, second = 30))
        val holder = createHolder(clock)
        backgroundScope.launch { holder.keepTime() }
        runCurrent()

        /* When */
        clock.time = amsterdam(2027, 1, 1, 0, 0)
        advanceTimeBy(30.seconds)
        runCurrent()

        /* Then */
        assertEquals(Greeting.WithYear(R.string.happy_year, 2027), holder.state.value.greeting)
    }

    @Test
    fun `on king's day the greeting says fijne koningsdag`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2027, 4, 27, 14, 0)))

        /* Then */
        assertEquals(Greeting.Plain(R.string.fijne_koningsdag), holder.state.value.greeting)
    }

    @Test
    fun `on king's day bunting hangs across the top`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2027, 4, 27, 14, 0)))

        /* Then */
        assertEquals(Celebration.Bunting, holder.state.value.celebration)
    }

    @Test
    fun `king's day lasts from midnight`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2027, 4, 27, 0, 0)))

        /* Then */
        assertEquals(Greeting.Plain(R.string.fijne_koningsdag), holder.state.value.greeting)
    }

    @Test
    fun `king's day ends at midnight`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2027, 4, 28, 0, 0)))

        /* Then */
        assertEquals(Greeting.Plain(R.string.good_night), holder.state.value.greeting)
    }

    @Test
    fun `the day before king's day is an ordinary day`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2027, 4, 26, 14, 0)))

        /* Then */
        assertEquals(Greeting.Plain(R.string.good_afternoon), holder.state.value.greeting)
    }

    @Test
    fun `when 27 april is a sunday king's day moves to the saturday before`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2025, 4, 26, 14, 0)))

        /* Then */
        assertEquals(Greeting.Plain(R.string.fijne_koningsdag), holder.state.value.greeting)
    }

    @Test
    fun `when 27 april is a sunday it is an ordinary day`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2025, 4, 27, 14, 0)))

        /* Then */
        assertEquals(Greeting.Plain(R.string.good_afternoon), holder.state.value.greeting)
    }

    @Test
    fun `an ordinary day has no fireworks or bunting`() {
        /* When */
        val holder = createHolder(FakeClock(amsterdam(2026, 10, 21, 21, 0)))

        /* Then */
        assertEquals(null, holder.state.value.celebration)
    }

    private fun amsterdam(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int = 0): ZonedDateTime = ZonedDateTime.of(year, month, day, hour, minute, second, 0, ZoneId.of("Europe/Amsterdam"))

    private fun createHolder(clock: Clock): HomeStateHolder = HomeStateHolder(
        config = HomeConfig.Enschede,
        installedApps = FakeInstalledApps(emptySet()),
        lastOpenedApp = FakeLastOpenedApp(),
        clock = clock,
        listener = RecordingEvents(),
    )
}
