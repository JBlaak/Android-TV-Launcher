package dev.jblaak.tvlauncher.sky

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SkySceneTest {

    @Test
    fun `on an october evening the sun stands low left of centre`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 17, 5), westWindow)

        /* Then */
        val sun = assertIs<Sun.Disc>(scene.sun)
        assertTrue(sun.position.x < 640, "x was ${sun.position.x}")
        assertTrue(sun.position.y in 560.0..600.0, "y was ${sun.position.y}")
    }

    @Test
    fun `in the morning the sun is behind the viewer and only lights the sky`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 9, 0), westWindow)

        /* Then */
        assertEquals(Sun.BehindViewer, scene.sun)
    }

    @Test
    fun `at night the sun is below the horizon`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 23, 0), westWindow)

        /* Then */
        assertEquals(Sun.BelowHorizon, scene.sun)
    }

    @Test
    fun `at midsummer the sun sets right of centre`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 6, 21, 21, 45), westWindow)

        /* Then */
        val sun = assertIs<Sun.Disc>(scene.sun)
        assertTrue(sun.position.x > 640, "x was ${sun.position.x}")
    }

    @Test
    fun `a sun near the horizon glows red-orange`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 17, 5), westWindow)

        /* Then */
        val sun = assertIs<Sun.Disc>(scene.sun)
        assertEquals(SunColors(glow = Rgb(255, 120, 50), core = Rgb(255, 214, 160)), sun.colors)
    }

    @Test
    fun `a sun high in the sky is near-white`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 6, 21, 17, 0), westWindow)

        /* Then */
        val sun = assertIs<Sun.Disc>(scene.sun)
        assertEquals(SunColors(glow = Rgb(255, 228, 170), core = Rgb(255, 252, 244)), sun.colors)
    }

    @Test
    fun `the warm glow follows the sun`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 17, 5), westWindow)

        /* Then */
        val sun = assertIs<Sun.Disc>(scene.sun)
        assertEquals(sun.position.x, scene.colors.warmCentre.x, 0.001)
    }

    @Test
    fun `the cool glow sits opposite the warm glow near the top`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 17, 5), westWindow)

        /* Then */
        assertEquals(ScreenPoint(1280 - scene.colors.warmCentre.x, 36.0), scene.colors.coolCentre)
    }

    @Test
    fun `the deepest night is still not black`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 12, 21, 0, 30), westWindow)

        /* Then */
        assertTrue(scene.colors.base.let { it.red + it.green + it.blue } > 40, "base was ${scene.colors.base}")
    }

    private fun amsterdam(year: Int, month: Int, day: Int, hour: Int, minute: Int): Instant = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZoneId.of("Europe/Amsterdam")).toInstant()

    private companion object {
        val westWindow = Window(location = Location(latitude = 52.2215, longitude = 6.8937), facing = 270.0)
    }
}
