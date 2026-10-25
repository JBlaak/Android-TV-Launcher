package dev.jblaak.tvlauncher.sky.internal

import dev.jblaak.tvlauncher.sky.Wind
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sin

/**
 * The wind as seen through the window: only its part across the view shows, [side] km/h, positive to the right
 * (v1 mock-up: windOnScreen; DESIGN.md, Wind).
 */
internal data class WindOnScreen(
    val side: Double,
    val speed: Double,
) {
    val direction: Int get() = if (side >= 0) 1 else -1

    /**
     * How far rain leans at [gust] times the wind: a drop falls at about 32 km/h, capped at ±55° so it stays
     * readable.
     */
    fun tilt(gust: Double): Double = Math.toDegrees(atan2(side * gust, 32.0)).coerceIn(-55.0, 55.0)
}

/**
 * The air moves toward `from + 180°`; `rel` is that heading relative to where the window looks, wrapped to
 * −180..180. A wind straight toward or away from the window has no sideways part, not a rounding error's worth.
 */
internal fun Wind.onScreen(facing: Double): WindOnScreen {
    val relative = ((from + 180 - facing) % 360 + 540) % 360 - 180
    val side = speed * sin(Math.toRadians(relative))
    return WindOnScreen(side = if (abs(side) < 1e-9) 0.0 else side, speed = speed)
}
