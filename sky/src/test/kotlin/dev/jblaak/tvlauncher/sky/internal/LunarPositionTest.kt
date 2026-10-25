package dev.jblaak.tvlauncher.sky.internal

import dev.jblaak.tvlauncher.sky.Location
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class LunarPositionTest {

    @Test
    fun `at 03 00 on 26 october 2026 the moon stands at azimuth 242 degrees`() {
        /* When */
        val moon = moonPosition(ZonedDateTime.of(2026, 10, 26, 3, 0, 0, 0, amsterdam).toInstant(), enschede)

        /* Then */
        assertEquals(242.0, moon.azimuth, 1.0)
    }

    @Test
    fun `at 03 00 on 26 october 2026 the moon stands at elevation 39 degrees`() {
        /* When */
        val moon = moonPosition(ZonedDateTime.of(2026, 10, 26, 3, 0, 0, 0, amsterdam).toInstant(), enschede)

        /* Then */
        assertEquals(39.0, moon.elevation, 1.0)
    }

    private companion object {
        val amsterdam: ZoneId = ZoneId.of("Europe/Amsterdam")
        val enschede = Location(latitude = 52.2215, longitude = 6.8937)
    }
}
