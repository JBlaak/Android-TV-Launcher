package dev.jblaak.tvlauncher.sky

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WindowGlassTest {

    @Test
    fun `rain leaves about 30 beads on the glass`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Rain)

        /* Then */
        assertEquals(30, scene.glass.beads.size)
    }

    @Test
    fun `a storm leaves beads on the glass`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Storm)

        /* Then */
        assertTrue(scene.glass.beads.isNotEmpty())
    }

    @Test
    fun `about a quarter of the beads slide down`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Rain)

        /* Then */
        assertEquals(8, scene.glass.beads.count { it.slide != null })
    }

    @Test
    fun `sliding beads slide out of step, each in its own slow loop`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Rain)

        /* Then */
        val slides = scene.glass.beads.mapNotNull { it.slide }
        assertTrue(slides.all { it.periodSeconds in 9.0..17.0 })
        assertEquals(slides.size, slides.map { it.phaseSeconds }.distinct().size)
    }

    @Test
    fun `beads are spread over the glass and stay out of its bottom edge`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Rain)

        /* Then */
        assertTrue(scene.glass.beads.all { it.topLeft.x in 0.0..1280.0 && it.topLeft.y in 0.0..0.92 * 720 })
    }

    @Test
    fun `beads are between 5 and 18 px wide and a little taller than wide`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Rain)

        /* Then */
        assertTrue(scene.glass.beads.all { it.width in 5.0..18.0 && it.height == it.width * 1.12 })
    }

    @Test
    fun `the beads sit in the same place every time`() {
        /* When */
        val first = skyScene(evening, westWindow, SkyWeather.Rain)
        val later = skyScene(evening.plusSeconds(3600), westWindow, SkyWeather.Rain)

        /* Then */
        assertEquals(first.glass.beads, later.glass.beads)
    }

    @Test
    fun `there are no beads without rain`() {
        /* When */
        val scenes = listOf(SkyWeather.Clear, SkyWeather.Warm, SkyWeather.Clouds, SkyWeather.Snow, SkyWeather.Fog)
            .map { skyScene(evening, westWindow, it) }

        /* Then */
        assertTrue(scenes.all { it.glass.beads.isEmpty() })
    }

    @Test
    fun `snow frosts the glass`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Snow)

        /* Then */
        assertTrue(scene.glass.frost)
    }

    @Test
    fun `there is no frost without snow`() {
        /* When */
        val scenes = listOf(SkyWeather.Clear, SkyWeather.Warm, SkyWeather.Clouds, SkyWeather.Rain, SkyWeather.Storm, SkyWeather.Fog)
            .map { skyScene(evening, westWindow, it) }

        /* Then */
        assertFalse(scenes.any { it.glass.frost })
    }

    @Test
    fun `a clear sky leaves the glass clean`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Clear)

        /* Then */
        assertEquals(WindowGlass.Clean, scene.glass)
    }

    private companion object {
        val westWindow = Window(location = Location(latitude = 52.2215, longitude = 6.8937), facing = 270.0)
        val evening: Instant = ZonedDateTime.of(2026, 10, 25, 17, 5, 0, 0, ZoneId.of("Europe/Amsterdam")).toInstant()
    }
}
