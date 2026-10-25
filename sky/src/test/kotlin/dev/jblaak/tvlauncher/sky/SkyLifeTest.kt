package dev.jblaak.tvlauncher.sky

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SkyLifeTest {

    @Test
    fun `shooting stars cross a clear night sky`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 23, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertTrue(scene.life.shootingStars)
    }

    @Test
    fun `shooting stars cross a warm night sky`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 7, 15, 1, 0), westWindow, SkyWeather.Warm)

        /* Then */
        assertTrue(scene.life.shootingStars)
    }

    @Test
    fun `there are no shooting stars behind clouds`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 23, 0), westWindow, SkyWeather.Clouds)

        /* Then */
        assertFalse(scene.life.shootingStars)
    }

    @Test
    fun `there are no shooting stars in the rain`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 23, 0), westWindow, SkyWeather.Rain)

        /* Then */
        assertFalse(scene.life.shootingStars)
    }

    @Test
    fun `there are no shooting stars while the sky is still light at dusk`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 18, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertFalse(scene.life.shootingStars)
    }

    @Test
    fun `there are no shooting stars by day`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 13, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertFalse(scene.life.shootingStars)
    }

    @Test
    fun `geese fly on an october evening`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 17, 5), westWindow, SkyWeather.Clear)

        /* Then */
        assertTrue(scene.life.geese)
    }

    @Test
    fun `geese fly in february`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 2, 15, 17, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertTrue(scene.life.geese)
    }

    @Test
    fun `geese fly in march`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 3, 15, 18, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertTrue(scene.life.geese)
    }

    @Test
    fun `geese fly in september`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 9, 15, 19, 30), westWindow, SkyWeather.Clear)

        /* Then */
        assertTrue(scene.life.geese)
    }

    @Test
    fun `geese fly in november`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 11, 15, 16, 30), westWindow, SkyWeather.Clear)

        /* Then */
        assertTrue(scene.life.geese)
    }

    @Test
    fun `geese stay away in january`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 1, 15, 16, 30), westWindow, SkyWeather.Clear)

        /* Then */
        assertFalse(scene.life.geese)
    }

    @Test
    fun `geese stay away in summer`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 8, 15, 20, 30), westWindow, SkyWeather.Clear)

        /* Then */
        assertFalse(scene.life.geese)
    }

    @Test
    fun `geese stay away in december`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 12, 15, 16, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertFalse(scene.life.geese)
    }

    @Test
    fun `geese stay away once it is dark`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 19, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertFalse(scene.life.geese)
    }

    @Test
    fun `geese stay away at midday in october`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 13, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertFalse(scene.life.geese)
    }

    @Test
    fun `geese stay away in the afternoon before the sun gets low`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 15, 0), westWindow, SkyWeather.Clear)

        /* Then */
        assertFalse(scene.life.geese)
    }

    @Test
    fun `geese stay away around sunrise`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 7, 30), westWindow, SkyWeather.Clear)

        /* Then */
        assertFalse(scene.life.geese)
    }

    @Test
    fun `geese fly through the rain`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 17, 5), westWindow, SkyWeather.Rain)

        /* Then */
        assertTrue(scene.life.geese)
    }

    @Test
    fun `geese stay away in a storm`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 17, 5), westWindow, SkyWeather.Storm)

        /* Then */
        assertFalse(scene.life.geese)
    }

    @Test
    fun `geese stay away in fog`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 17, 5), westWindow, SkyWeather.Fog)

        /* Then */
        assertFalse(scene.life.geese)
    }

    private fun amsterdam(year: Int, month: Int, day: Int, hour: Int, minute: Int): Instant = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZoneId.of("Europe/Amsterdam")).toInstant()

    private companion object {
        val westWindow = Window(location = Location(latitude = 52.2215, longitude = 6.8937), facing = 270.0)
    }
}
