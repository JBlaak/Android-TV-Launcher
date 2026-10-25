package dev.jblaak.tvlauncher.sky

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SkyWeatherTest {

    @Test
    fun `rain pulls the sky toward grey-blue`() {
        /* When */
        val scene = skyScene(evening, westWindow, Weather.Rain)

        /* Then */
        assertEquals(clearEvening.colors.base.towards(Rgb(22, 30, 42), 0.6), scene.colors.base)
    }

    @Test
    fun `rain greys the warm glow`() {
        /* When */
        val scene = skyScene(evening, westWindow, Weather.Rain)

        /* Then */
        assertEquals(clearEvening.colors.warm.towards(Rgb(90, 115, 145), 0.6), scene.colors.warm)
    }

    @Test
    fun `rain greys the cool glow`() {
        /* When */
        val scene = skyScene(evening, westWindow, Weather.Rain)

        /* Then */
        assertEquals(clearEvening.colors.cool.towards(Rgb(90, 115, 145), 0.6), scene.colors.cool)
    }

    @Test
    fun `rain dims the glows`() {
        /* When */
        val scene = skyScene(evening, westWindow, Weather.Rain)

        /* Then */
        assertEquals(clearEvening.colors.strength * 0.6, scene.colors.strength, 1e-9)
    }

    @Test
    fun `snow cools the sky`() {
        /* When */
        val scene = skyScene(evening, westWindow, Weather.Snow)

        /* Then */
        assertEquals(clearEvening.colors.base.towards(Rgb(50, 58, 76), 0.55), scene.colors.base)
    }

    @Test
    fun `warmth turns the glows amber`() {
        /* When */
        val scene = skyScene(evening, westWindow, Weather.Warm)

        /* Then */
        assertEquals(clearEvening.colors.warm.towards(Rgb(255, 160, 70), 0.35), scene.colors.warm)
    }

    @Test
    fun `warmth strengthens the glows`() {
        /* When */
        val scene = skyScene(evening, westWindow, Weather.Warm)

        /* Then */
        assertEquals(clearEvening.colors.strength * 1.2, scene.colors.strength, 1e-9)
    }

    @Test
    fun `clouds grey the sky`() {
        /* When */
        val scene = skyScene(evening, westWindow, Weather.Cloudy)

        /* Then */
        assertEquals(clearEvening.colors.base.towards(Rgb(40, 44, 52), 0.5), scene.colors.base)
    }

    @Test
    fun `a storm darkens the sky toward violet-grey`() {
        /* When */
        val scene = skyScene(evening, westWindow, Weather.Storm)

        /* Then */
        assertEquals(clearEvening.colors.base.towards(Rgb(16, 16, 28), 0.7), scene.colors.base)
    }

    @Test
    fun `fog pales the sky`() {
        /* When */
        val scene = skyScene(evening, westWindow, Weather.Fog)

        /* Then */
        assertEquals(clearEvening.colors.base.towards(Rgb(46, 50, 58), 0.6), scene.colors.base)
    }

    @Test
    fun `a storm leaves the warm glow where the sun puts it`() {
        /* When */
        val scene = skyScene(evening, westWindow, Weather.Storm)

        /* Then */
        assertEquals(clearEvening.colors.warmCentre, scene.colors.warmCentre)
    }

    @Test
    fun `in warm weather the sun burns brighter`() {
        /* When */
        val scene = skyScene(evening, westWindow, Weather.Warm)

        /* Then */
        val sun = assertIs<Sun.Disc>(scene.sun)
        assertEquals((clearEvening.sun as Sun.Disc).intensity * 1.15, sun.intensity, 1e-9)
    }

    @Test
    fun `behind clouds a sun fully in view is only a soft glow`() {
        /* When */
        val scene = skyScene(evening, westWindow, Weather.Cloudy)

        /* Then */
        assertEquals(0.18, assertIs<Sun.Diffuse>(scene.sun).opacity, 1e-9)
    }

    @Test
    fun `in rain the sun's glow is fainter`() {
        /* When */
        val scene = skyScene(evening, westWindow, Weather.Rain)

        /* Then */
        assertEquals(0.08, assertIs<Sun.Diffuse>(scene.sun).opacity, 1e-9)
    }

    @Test
    fun `the sun's glow sits where the sun is`() {
        /* When */
        val scene = skyScene(evening, westWindow, Weather.Fog)

        /* Then */
        assertEquals((clearEvening.sun as Sun.Disc).position, assertIs<Sun.Diffuse>(scene.sun).position)
    }

    @Test
    fun `a storm hides the sun`() {
        /* When */
        val scene = skyScene(evening, westWindow, Weather.Storm)

        /* Then */
        assertEquals(Sun.Hidden, scene.sun)
    }

    @Test
    fun `snow hides the sun`() {
        /* When */
        val scene = skyScene(evening, westWindow, Weather.Snow)

        /* Then */
        assertEquals(Sun.Hidden, scene.sun)
    }

    @Test
    fun `clouds hide the light of a sun behind the viewer`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 9, 0), westWindow, Weather.Cloudy)

        /* Then */
        assertEquals(Sun.Hidden, scene.sun)
    }

    @Test
    fun `in warm weather a sun behind the viewer still lights the sky`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 9, 0), westWindow, Weather.Warm)

        /* Then */
        assertEquals(Sun.BehindViewer, scene.sun)
    }

    @Test
    fun `at night rain leaves the sun below the horizon`() {
        /* When */
        val scene = skyScene(amsterdam(2026, 10, 25, 23, 0), westWindow, Weather.Rain)

        /* Then */
        assertEquals(Sun.BelowHorizon, scene.sun)
    }

    private fun Rgb.towards(other: Rgb, fraction: Double): Rgb = Rgb(
        red = (red + (other.red - red) * fraction).roundToInt(),
        green = (green + (other.green - green) * fraction).roundToInt(),
        blue = (blue + (other.blue - blue) * fraction).roundToInt(),
    )

    private companion object {
        val westWindow = Window(location = Location(latitude = 52.2215, longitude = 6.8937), facing = 270.0)

        fun amsterdam(year: Int, month: Int, day: Int, hour: Int, minute: Int): Instant = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZoneId.of("Europe/Amsterdam")).toInstant()

        val evening: Instant = amsterdam(2026, 10, 25, 17, 5)

        val clearEvening: SkyScene = skyScene(evening, westWindow, Weather.Clear)
    }
}
