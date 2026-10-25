package dev.jblaak.tvlauncher.sky

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MoonTest {

    @Test
    fun `at 03 00 on 26 october the full moon stands high in the south-west`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 26, 3, 0), westWindow, SkyWeather.Clear)

        /* Then */
        val moon = assertIs<Moon.Disc>(scene.moon)
        assertEquals(ScreenPoint(640.0 + (242 - 270) * 8, 600.0 - 39 * 8), moon.position, toleranceOfOneDegree)
    }

    @Test
    fun `at 03 00 on 26 october the moon is full`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 26, 3, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertEquals(1.0, assertIs<Moon.Disc>(scene.moon).phase.litFraction, 0.01)
    }

    @Test
    fun `at 13 00 on 25 october there is no moon`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 13, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertEquals(Moon.Hidden, scene.moon)
    }

    @Test
    fun `a moon that is up but outside the view is not shown`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 17, 5), westWindow, SkyWeather.Clear)

        /* Then */
        assertEquals(Moon.Hidden, scene.moon)
    }

    @Test
    fun `at 23 00 at midsummer the moon is about 42 percent lit`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 6, 21, 23, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertEquals(0.42, assertIs<Moon.Disc>(scene.moon).phase.litFraction, 0.02)
    }

    @Test
    fun `at midsummer the moon is waxing`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 6, 21, 23, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertTrue(assertIs<Moon.Disc>(scene.moon).phase.waxing)
    }

    @Test
    fun `a week after full moon the moon is waning`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 11, 1, 7, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertFalse(assertIs<Moon.Disc>(scene.moon).phase.waxing)
    }

    @Test
    fun `the moon's age counts the days since the last new moon`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 6, 21, 23, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertEquals(6.6, assertIs<Moon.Disc>(scene.moon).phase.ageInDays, 0.1)
    }

    @Test
    fun `a moon less than 3 percent lit is not shown`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 11, 8, 14, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertEquals(Moon.Hidden, scene.moon)
    }

    @Test
    fun `on a clear night the moon shows at full opacity`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 26, 3, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertEquals(1.0, assertIs<Moon.Disc>(scene.moon).opacity, 0.001)
    }

    @Test
    fun `on a warm night the moon shows at full opacity`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 26, 3, 0), westWindow, SkyWeather.Warm)

        /* Then */
        assertEquals(1.0, assertIs<Moon.Disc>(scene.moon).opacity, 0.001)
    }

    @Test
    fun `while the sun is up the moon shows at 55 percent`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 6, 21, 21, 45), westWindow, SkyWeather.Clear)

        /* Then */
        assertEquals(0.55, assertIs<Moon.Disc>(scene.moon).opacity, 0.001)
    }

    @Test
    fun `behind clouds the moon shows at 45 percent`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 26, 3, 0), westWindow, SkyWeather.Clouds)

        /* Then */
        assertEquals(0.45, assertIs<Moon.Disc>(scene.moon).opacity, 0.001)
    }

    @Test
    fun `behind clouds the daytime moon also shows at 45 percent`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 6, 21, 21, 45), westWindow, SkyWeather.Clouds)

        /* Then */
        assertEquals(0.45, assertIs<Moon.Disc>(scene.moon).opacity, 0.001)
    }

    @Test
    fun `the moon fades toward the edge of the view`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 23, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertEquals(0.72, assertIs<Moon.Disc>(scene.moon).opacity, 0.01)
    }

    @Test
    fun `in rain at night the moon is only a soft glow, scaled by how much is lit`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 6, 21, 23, 0), westWindow, SkyWeather.Rain)

        /* Then */
        assertEquals(0.14 * 0.418, assertIs<Moon.Glow>(scene.moon).opacity, 0.002)
    }

    @Test
    fun `in snow at night the moon is only a soft glow`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 26, 3, 0), westWindow, SkyWeather.Snow)

        /* Then */
        assertIs<Moon.Glow>(scene.moon)
    }

    @Test
    fun `in fog at night the moon is only a soft glow`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 26, 3, 0), westWindow, SkyWeather.Fog)

        /* Then */
        assertIs<Moon.Glow>(scene.moon)
    }

    @Test
    fun `in a storm at night the moon is only a soft glow`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 26, 3, 0), westWindow, SkyWeather.Storm)

        /* Then */
        assertIs<Moon.Glow>(scene.moon)
    }

    @Test
    fun `the moon's glow sits where the moon is`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 26, 3, 0), westWindow, SkyWeather.Rain)

        /* Then */
        val disc = assertIs<Moon.Disc>(skyScene(amsterdam(2026, 10, 26, 3, 0), westWindow, SkyWeather.Clear).moon)
        assertEquals(disc.position, assertIs<Moon.Glow>(scene.moon).position)
    }

    @Test
    fun `in rain while the sun is up there is no moon at all`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 6, 21, 21, 45), westWindow, SkyWeather.Rain)

        /* Then */
        assertEquals(Moon.Hidden, scene.moon)
    }

    private fun assertEquals(expected: ScreenPoint, actual: ScreenPoint, tolerance: Double) {
        assertEquals(expected.x, actual.x, tolerance, "x")
        assertEquals(expected.y, actual.y, tolerance, "y")
    }

    private fun amsterdam(year: Int, month: Int, day: Int, hour: Int, minute: Int): Instant = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZoneId.of("Europe/Amsterdam")).toInstant()

    private companion object {
        val westWindow = Window(location = Location(latitude = 52.2215, longitude = 6.8937), facing = 270.0)
        const val toleranceOfOneDegree = 8.0
    }
}
