package dev.jblaak.tvlauncher.sky

import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SunEventTest {

    @Test
    fun `on 25 october the sun rises at 07 16`() {
        /* When */
        val event = nextSunEvent(amsterdam(2026, 10, 25, 4, 0), enschede)

        /* Then */
        assertIs<SunEvent.Sunrise>(event)
        assertAbout(amsterdam(2026, 10, 25, 7, 16), event.at)
    }

    @Test
    fun `on 25 october the sun sets at 17 18`() {
        /* When */
        val event = nextSunEvent(amsterdam(2026, 10, 25, 12, 0), enschede)

        /* Then */
        assertIs<SunEvent.Sunset>(event)
        assertAbout(amsterdam(2026, 10, 25, 17, 18), event.at)
    }

    @Test
    fun `at midsummer the sun rises at 05 11`() {
        /* When */
        val event = nextSunEvent(amsterdam(2026, 6, 21, 0, 30), enschede)

        /* Then */
        assertIs<SunEvent.Sunrise>(event)
        assertAbout(amsterdam(2026, 6, 21, 5, 11), event.at)
    }

    @Test
    fun `at midsummer the sun sets at 21 58`() {
        /* When */
        val event = nextSunEvent(amsterdam(2026, 6, 21, 12, 0), enschede)

        /* Then */
        assertIs<SunEvent.Sunset>(event)
        assertAbout(amsterdam(2026, 6, 21, 21, 58), event.at)
    }

    @Test
    fun `at midwinter the sun rises at 08 40`() {
        /* When */
        val event = nextSunEvent(amsterdam(2026, 12, 21, 6, 0), enschede)

        /* Then */
        assertIs<SunEvent.Sunrise>(event)
        assertAbout(amsterdam(2026, 12, 21, 8, 40), event.at)
    }

    @Test
    fun `at midwinter the sun sets at 16 22`() {
        /* When */
        val event = nextSunEvent(amsterdam(2026, 12, 21, 12, 0), enschede)

        /* Then */
        assertIs<SunEvent.Sunset>(event)
        assertAbout(amsterdam(2026, 12, 21, 16, 22), event.at)
    }

    @Test
    fun `after sunset the next event is tomorrow's sunrise`() {
        /* When */
        val event = nextSunEvent(amsterdam(2026, 10, 25, 20, 0), enschede)

        /* Then */
        assertIs<SunEvent.Sunrise>(event)
        assertAbout(amsterdam(2026, 10, 26, 7, 17), event.at)
    }

    private fun assertAbout(expected: ZonedDateTime, actual: ZonedDateTime) {
        val difference = Duration.between(expected, actual).abs()
        assertTrue(difference <= Duration.ofMinutes(2), "expected $expected ±2 min, was $actual")
    }

    private fun amsterdam(year: Int, month: Int, day: Int, hour: Int, minute: Int): ZonedDateTime = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZoneId.of("Europe/Amsterdam"))

    private companion object {
        val enschede = Location(latitude = 52.2215, longitude = 6.8937)
    }
}
