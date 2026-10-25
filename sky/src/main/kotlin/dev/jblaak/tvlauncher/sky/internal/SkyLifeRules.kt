package dev.jblaak.tvlauncher.sky.internal

import dev.jblaak.tvlauncher.sky.SkyLife
import dev.jblaak.tvlauncher.sky.SkyWeather
import java.time.Instant
import java.time.Month
import java.time.ZoneOffset

/**
 * Shooting stars need a clear, dark sky (sun below −8°). Geese migrate in February, March and September to
 * November around sunset, but not in a storm or fog (canvas copy, DockNight wildlife).
 *
 * "Around sunset" is the evening band from the sun at 10° in the west down to −6°, the end of civil dusk: about an
 * hour and a half on an October evening. Neither DESIGN.md nor the canvas gives a band; the canvas only asks for a
 * light sky (sun above −6°), which on its own also lets the geese fly at noon and at sunrise.
 *
 * The month is taken in UTC: the window has no time zone, and around midnight, where UTC and local months can
 * differ, the sun is always far below −6° in Enschede, so the geese rule comes out the same.
 */
internal fun skyLife(at: Instant, sun: Horizontal, weather: SkyWeather): SkyLife = SkyLife(
    shootingStars = weather in setOf(SkyWeather.Clear, SkyWeather.Warm) && sun.elevation < -8,
    geese = at.atOffset(ZoneOffset.UTC).month in migrationMonths &&
        sun.isAroundSunset() &&
        weather != SkyWeather.Storm &&
        weather != SkyWeather.Fog,
)

private fun Horizontal.isAroundSunset(): Boolean = azimuth > 180 && elevation in -6.0..10.0

private val migrationMonths = setOf(Month.FEBRUARY, Month.MARCH, Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER)
