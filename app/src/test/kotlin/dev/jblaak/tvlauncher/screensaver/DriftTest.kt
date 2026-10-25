package dev.jblaak.tvlauncher.screensaver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import kotlin.math.hypot

class DriftTest {

    private val evening = LocalDateTime.of(2026, 10, 25, 17, 5)

    @Test
    fun `every spot lies inside the free space`() {
        /* When */
        val spots = (0L until 24 * 60).map { driftSpot(evening.plusMinutes(it)) }

        /* Then */
        assertTrue(spots.all { it.x in 0f..1f && it.y in 0f..1f })
    }

    @Test
    fun `the label moves far every minute`() {
        /* When */
        val distances = (0L until 24 * 60).map {
            val from = driftSpot(evening.plusMinutes(it))
            val to = driftSpot(evening.plusMinutes(it + 1))
            hypot(to.x - from.x, to.y - from.y)
        }

        /* Then */
        assertTrue(distances.min() > 0.3f)
    }

    @Test
    fun `within an hour the label visits every quarter of the screen`() {
        /* When */
        val quarters = (0L until 60).map { driftSpot(evening.plusMinutes(it)) }.map { (it.x < 0.5f) to (it.y < 0.5f) }.toSet()

        /* Then */
        assertEquals(4, quarters.size)
    }

    @Test
    fun `the label stays put within a minute`() {
        /* When */
        val start = driftSpot(evening)
        val later = driftSpot(evening.plusSeconds(59))

        /* Then */
        assertEquals(start, later)
    }
}
