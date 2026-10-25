package dev.jblaak.tvlauncher.sky.internal

import dev.jblaak.tvlauncher.sky.Rgb
import kotlin.test.Test
import kotlin.test.assertEquals

class SkyKeyframesTest {

    @Test
    fun `at -12 degrees the sky has the night colours`() {
        /* When */
        val colors = skyKeyframe(-12.0)

        /* Then */
        assertEquals(SkyKeyframe(Rgb(16, 18, 36), Rgb(80, 70, 170), Rgb(40, 100, 140), 0.40), colors)
    }

    @Test
    fun `at -90 degrees the sky has the deepest night colours`() {
        /* When */
        val colors = skyKeyframe(-90.0)

        /* Then */
        assertEquals(SkyKeyframe(Rgb(13, 14, 26), Rgb(70, 60, 150), Rgb(30, 90, 120), 0.35), colors)
    }

    @Test
    fun `at -1 degrees the sky has the sunset colours`() {
        /* When */
        val colors = skyKeyframe(-1.0)

        /* Then */
        assertEquals(SkyKeyframe(Rgb(50, 30, 58), Rgb(255, 125, 85), Rgb(205, 85, 150), 0.60), colors)
    }

    @Test
    fun `between two keyframes the colours blend and round per channel`() {
        /* When */
        val colors = skyKeyframe(-3.5)

        /* Then */
        assertEquals(Rgb(38, 30, 61), colors.base)
    }

    @Test
    fun `between two keyframes the glow strength blends`() {
        /* When */
        val colors = skyKeyframe(-3.5)

        /* Then */
        assertEquals(0.575, colors.strength, 0.0001)
    }

    @Test
    fun `at 90 degrees the sky has the day colours`() {
        /* When */
        val colors = skyKeyframe(90.0)

        /* Then */
        assertEquals(SkyKeyframe(Rgb(40, 60, 88), Rgb(145, 200, 245), Rgb(155, 225, 215), 0.50), colors)
    }
}
