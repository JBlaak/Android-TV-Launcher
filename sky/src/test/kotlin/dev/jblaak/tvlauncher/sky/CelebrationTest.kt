package dev.jblaak.tvlauncher.sky

import kotlin.math.hypot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CelebrationTest {

    @Test
    fun `fireworks go off in five bursts`() {
        /* When */
        val bursts = fireworks()

        /* Then */
        assertEquals(5, bursts.size)
    }

    @Test
    fun `each burst throws twenty sparks`() {
        /* When */
        val bursts = fireworks()

        /* Then */
        assertEquals(List(5) { 20 }, bursts.map { it.sparks.size })
    }

    @Test
    fun `the bursts go off over the top half of the sky`() {
        /* When */
        val bursts = fireworks()

        /* Then */
        assertTrue(bursts.all { it.centre.x in 180.0..1100.0 && it.centre.y in 90.0..310.0 }, "centres were ${bursts.map { it.centre }}")
    }

    @Test
    fun `sparks fly 90 to 130 pixels out`() {
        /* When */
        val sparks = fireworks().flatMap { it.sparks }

        /* Then */
        assertTrue(sparks.all { hypot(it.x, it.y) in 89.5..130.5 })
    }

    @Test
    fun `fireworks look the same every time`() {
        /* When */
        val first = fireworks()

        /* Then */
        assertEquals(first, fireworks())
    }

    @Test
    fun `bunting hangs 24 flags across the window`() {
        /* When */
        val flags = bunting()

        /* Then */
        assertEquals(24, flags.size)
    }

    @Test
    fun `the flags run red, white, blue and orange`() {
        /* When */
        val flags = bunting()

        /* Then */
        assertEquals(
            listOf(Rgb(0xE8, 0x11, 0x2D), Rgb(0xFF, 0xFF, 0xFF), Rgb(0x21, 0x46, 0x8B), Rgb(0xFF, 0x7F, 0x00)),
            flags.take(4).map { it.color },
        )
    }

    @Test
    fun `the rope sags lowest in the middle`() {
        /* When */
        val flags = bunting()

        /* Then */
        val lowest = flags.maxBy { it.anchor.y }
        assertTrue(lowest.anchor.x in 580.0..700.0, "lowest flag at ${lowest.anchor}")
    }

    @Test
    fun `the flags tilt down toward the middle`() {
        /* When */
        val flags = bunting()

        /* Then */
        assertTrue(flags.first().rotation > 0 && flags.last().rotation < 0)
    }
}
