package dev.jblaak.tvlauncher.sky.internal

import dev.jblaak.tvlauncher.sky.ScreenPoint
import kotlin.test.Test
import kotlin.test.assertEquals

class WindowProjectionTest {

    @Test
    fun `a body due west on the horizon sits in the centre on the horizon line`() {
        /* When */
        val position = project(Horizontal(elevation = 0.0, azimuth = 270.0), facing = 270.0)

        /* Then */
        assertEquals(ScreenPoint(640.0, 600.0), position.point)
    }

    @Test
    fun `one degree is eight pixels`() {
        /* When */
        val position = project(Horizontal(elevation = 10.0, azimuth = 260.0), facing = 270.0)

        /* Then */
        assertEquals(ScreenPoint(560.0, 520.0), position.point)
    }

    @Test
    fun `a body 100 degrees off the view is still fully visible`() {
        /* When */
        val position = project(Horizontal(elevation = 10.0, azimuth = 170.0), facing = 270.0)

        /* Then */
        assertEquals(1.0, position.visibility, 0.0001)
    }

    @Test
    fun `a body 120 degrees off the view is fading out`() {
        /* When */
        val position = project(Horizontal(elevation = 10.0, azimuth = 30.0), facing = 270.0)

        /* Then */
        assertEquals(140.0 / 300.0, position.visibility, 0.0001)
    }

    @Test
    fun `a body behind the viewer is not visible`() {
        /* When */
        val position = project(Horizontal(elevation = 10.0, azimuth = 90.0), facing = 270.0)

        /* Then */
        assertEquals(0.0, position.visibility, 0.0001)
    }

    @Test
    fun `a body more than a degree below the horizon is not visible`() {
        /* When */
        val position = project(Horizontal(elevation = -1.5, azimuth = 270.0), facing = 270.0)

        /* Then */
        assertEquals(0.0, position.visibility, 0.0001)
    }
}
