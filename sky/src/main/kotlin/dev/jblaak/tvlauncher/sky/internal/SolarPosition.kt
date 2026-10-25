package dev.jblaak.tvlauncher.sky.internal

import dev.jblaak.tvlauncher.sky.Location
import java.time.Instant
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * A position in the local sky, in degrees: [elevation] above the horizon, [azimuth] clockwise from north.
 */
internal data class Horizontal(
    val elevation: Double,
    val azimuth: Double,
)

/**
 * Low-precision solar position (Astronomical Almanac, about 0.01° over 1950-2050), as in the v1 mock-up
 * (docs/mockups/dock-v1.dc.html, sunPos).
 */
internal fun sunPosition(instant: Instant, location: Location): Horizontal {
    val d = daysSinceJ2000(instant)
    val g = Math.toRadians(357.529 + 0.98560028 * d)
    val eclipticLongitude = Math.toRadians(280.459 + 0.98564736 * d + 1.915 * sin(g) + 0.02 * sin(2 * g))
    val obliquity = Math.toRadians(23.439 - 0.00000036 * d)
    val rightAscension = atan2(cos(obliquity) * sin(eclipticLongitude), cos(eclipticLongitude))
    val declination = asin(sin(obliquity) * sin(eclipticLongitude))
    return horizontal(d, rightAscension, declination, location)
}

/**
 * Days since J2000.0 (2000-01-01 12:00 UTC).
 */
internal fun daysSinceJ2000(instant: Instant): Double = instant.toEpochMilli() / 86_400_000.0 - 10957.5

/**
 * Equatorial to horizontal coordinates, with the local sidereal time from GMST plus the longitude.
 */
internal fun horizontal(d: Double, rightAscension: Double, declination: Double, location: Location): Horizontal {
    val latitude = Math.toRadians(location.latitude)
    val siderealTime = Math.toRadians((18.697374558 + 24.06570982441908 * d) * 15 + location.longitude)
    val hourAngle = siderealTime - rightAscension
    val elevation = asin(sin(latitude) * sin(declination) + cos(latitude) * cos(declination) * cos(hourAngle))
    val azimuth = atan2(sin(hourAngle), cos(hourAngle) * sin(latitude) - tan(declination) * cos(latitude))
    return Horizontal(
        elevation = Math.toDegrees(elevation),
        azimuth = (Math.toDegrees(azimuth) + 180).mod(360.0),
    )
}
