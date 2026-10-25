package dev.jblaak.tvlauncher.sky.internal

import dev.jblaak.tvlauncher.sky.SkyLife
import java.time.Instant
import java.time.Month
import java.time.ZoneOffset

/**
 * Shooting stars need a clear, dark sky (sun below −8°). Geese migrate in February, March and September to
 * November while the sky is light (sun above −6°), but not in a storm or fog (canvas copy, DockNight wildlife).
 *
 * The month is taken in UTC: the window has no time zone, and around midnight, where UTC and local months can
 * differ, the sun is always far below −6° in Enschede, so the geese rule comes out the same.
 */
internal fun skyLife(at: Instant, sunElevation: Double, weather: SkyWeather): SkyLife = SkyLife(
    shootingStars = weather in setOf(SkyWeather.Clear, SkyWeather.Warm) && sunElevation < -8,
    geese = at.atOffset(ZoneOffset.UTC).month in migrationMonths &&
        sunElevation > -6 &&
        weather != SkyWeather.Storm &&
        weather != SkyWeather.Fog,
)

private val migrationMonths = setOf(Month.FEBRUARY, Month.MARCH, Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER)
