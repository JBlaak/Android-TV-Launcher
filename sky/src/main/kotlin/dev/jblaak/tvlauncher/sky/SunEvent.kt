package dev.jblaak.tvlauncher.sky

import dev.jblaak.tvlauncher.sky.internal.sunPosition
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

sealed interface SunEvent {
    val at: ZonedDateTime

    data class Sunrise(override val at: ZonedDateTime) : SunEvent

    data class Sunset(override val at: ZonedDateTime) : SunEvent
}

/**
 * Today's sunrise if it's still to come, else today's sunset, else tomorrow's sunrise, in [now]'s time zone.
 * Null only where the sun doesn't rise or set that day.
 */
fun nextSunEvent(now: ZonedDateTime, location: Location): SunEvent? {
    val today = sunCrossings(now.toLocalDate(), now.zone, location)
    val tomorrow = sunCrossings(now.toLocalDate().plusDays(1), now.zone, location)
    return when {
        today.sunrise != null && now.isBefore(today.sunrise) -> SunEvent.Sunrise(today.sunrise)
        today.sunset != null && now.isBefore(today.sunset) -> SunEvent.Sunset(today.sunset)
        tomorrow.sunrise != null -> SunEvent.Sunrise(tomorrow.sunrise)
        else -> null
    }
}

private data class SunCrossings(
    val sunrise: ZonedDateTime?,
    val sunset: ZonedDateTime?,
)

/**
 * Sunrise and sunset are where the sun's centre crosses −0.833° (refraction plus the sun's radius). The local day
 * is scanned minute by minute and each crossing is interpolated between two minutes.
 */
private fun sunCrossings(day: LocalDate, zone: ZoneId, location: Location): SunCrossings {
    val start = day.atStartOfDay(zone).toInstant()
    val minutes = Duration.between(start, day.plusDays(1).atStartOfDay(zone).toInstant()).toMinutes()
    var sunrise: Instant? = null
    var sunset: Instant? = null
    var previous = sunPosition(start, location).elevation + 0.833
    for (minute in 1..minutes) {
        val instant = start.plusSeconds(minute * 60)
        val current = sunPosition(instant, location).elevation + 0.833
        val crossing = instant.minusMillis((60_000 * current / (current - previous)).toLong())
        if (previous < 0 && current >= 0 && sunrise == null) sunrise = crossing
        if (previous >= 0 && current < 0 && sunset == null) sunset = crossing
        previous = current
    }
    return SunCrossings(sunrise = sunrise?.atZone(zone), sunset = sunset?.atZone(zone))
}
