package dev.jblaak.tvlauncher.sky.internal

import dev.jblaak.tvlauncher.sky.Location
import dev.jblaak.tvlauncher.sky.MoonPhase
import java.time.Duration
import java.time.Instant
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Low-precision lunar position, about 1°: mean longitude, mean anomaly and argument of latitude with only the main
 * 6.289° longitude and 5.128° latitude terms (docs/DESIGN.md, Moon; the canvas's moonPos).
 */
internal fun moonPosition(instant: Instant, location: Location): Horizontal {
    val d = daysSinceJ2000(instant)
    val meanLongitude = Math.toRadians(218.316 + 13.176396 * d)
    val meanAnomaly = Math.toRadians(134.963 + 13.064993 * d)
    val argumentOfLatitude = Math.toRadians(93.272 + 13.229350 * d)
    val longitude = meanLongitude + Math.toRadians(6.289) * sin(meanAnomaly)
    val latitude = Math.toRadians(5.128) * sin(argumentOfLatitude)
    val obliquity = Math.toRadians(23.4397)
    val rightAscension = atan2(sin(longitude) * cos(obliquity) - tan(latitude) * sin(obliquity), cos(longitude))
    val declination = asin(sin(latitude) * cos(obliquity) + cos(latitude) * sin(obliquity) * sin(longitude))
    return horizontal(d, rightAscension, declination, location)
}

/**
 * The phase from the moon's age since the new moon of 2000-01-06 18:14 UTC. The lit fraction follows from the
 * age as (1 − cos(2π·age/synodic)) / 2.
 */
internal fun moonPhase(instant: Instant): MoonPhase {
    val synodicMonth = 29.530588853
    val days = Duration.between(Instant.parse("2000-01-06T18:14:00Z"), instant).toMillis() / 86_400_000.0
    val age = days.mod(synodicMonth)
    return MoonPhase(
        ageInDays = age,
        litFraction = (1 - cos(2 * PI * age / synodicMonth)) / 2,
        waxing = age < synodicMonth / 2,
    )
}
