package dev.jblaak.tvlauncher.screensaver

import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.floor

/**
 * Where the screensaver's label sits, as a fraction of the space it can move in: 0 is the left or top edge, 1 the
 * right or bottom edge.
 */
internal data class DriftSpot(val x: Float, val y: Float)

/**
 * The label's spot during the minute of [at]. Each minute takes the next point of the R2 sequence (Roberts, "The
 * unreasonable effectiveness of quasirandom sequences"), so the label jumps about half the screen every minute and
 * covers the whole screen evenly instead of returning to a few spots.
 */
internal fun driftSpot(at: LocalDateTime): DriftSpot {
    val minute = at.toEpochSecond(ZoneOffset.UTC) / 60
    return DriftSpot(x = fraction(minute * 0.7548776662466927), y = fraction(minute * 0.5698402909980532))
}

private fun fraction(value: Double): Float = (value - floor(value)).toFloat()
