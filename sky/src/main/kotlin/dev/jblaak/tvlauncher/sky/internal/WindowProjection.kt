package dev.jblaak.tvlauncher.sky.internal

import dev.jblaak.tvlauncher.sky.ScreenPoint
import kotlin.math.abs

/**
 * Where a body shows through the window, and how much of it: [visibility] fades from 1 to 0 toward the edges of
 * the view and is 0 once the body is more than a degree below the horizon.
 */
internal data class WindowPosition(
    val point: ScreenPoint,
    val visibility: Double,
)

/**
 * The window projection from docs/DESIGN.md: 8 px per degree on the 1280×720 reference, so the view is 160° wide,
 * with the horizon at y = 600 behind the dock.
 */
internal fun project(position: Horizontal, facing: Double): WindowPosition {
    val offset = (position.azimuth - facing + 540).mod(360.0) - 180
    val x = 640 + offset * 8
    val y = 600 - position.elevation * 8
    val edge = ((1100 - abs(x - 640)) / 300).coerceIn(0.0, 1.0)
    return WindowPosition(
        point = ScreenPoint(x, y),
        visibility = if (position.elevation > -1) edge else 0.0,
    )
}
