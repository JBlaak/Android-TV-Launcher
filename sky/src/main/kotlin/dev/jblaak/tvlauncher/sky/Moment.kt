package dev.jblaak.tvlauncher.sky

import java.time.DayOfWeek
import java.time.Month
import java.time.ZonedDateTime

/**
 * A special day that gets a special touch (whimsy "moments", design canvas `moment()`).
 */
sealed interface Moment {

    /**
     * New Year's Eve from 23:00, counting down to [comingYear].
     */
    data class NewYearsEve(val comingYear: Int) : Moment

    /**
     * New Year's Day until 02:00.
     */
    data class NewYearsDay(val year: Int) : Moment

    /**
     * Koningsdag: 27 April, or the 26th when the 27th is a Sunday.
     */
    data object KingsDay : Moment

    data object ClocksWentBack : Moment

    data object ClocksWentForward : Moment
}

/**
 * The moment at local time [at], if any. The first match wins: Oud & Nieuw, Koningsdag, then a clock change, which is
 * a UTC offset that differs from the one 24 hours earlier.
 */
fun moment(at: ZonedDateTime): Moment? = when {
    at.month == Month.DECEMBER && at.dayOfMonth == 31 && at.hour >= 23 -> Moment.NewYearsEve(at.year + 1)
    at.month == Month.JANUARY && at.dayOfMonth == 1 && at.hour < 2 -> Moment.NewYearsDay(at.year)
    at.isKingsDay() -> Moment.KingsDay
    else -> clockChange(at)
}

private fun ZonedDateTime.isKingsDay(): Boolean = month == Month.APRIL &&
    ((dayOfMonth == 27 && dayOfWeek != DayOfWeek.SUNDAY) || (dayOfMonth == 26 && dayOfWeek == DayOfWeek.SATURDAY))

private fun clockChange(at: ZonedDateTime): Moment? {
    val now = at.offset.totalSeconds
    val dayBefore = at.zone.rules.getOffset(at.toInstant().minusSeconds(24 * 60 * 60)).totalSeconds
    return when {
        now < dayBefore -> Moment.ClocksWentBack
        now > dayBefore -> Moment.ClocksWentForward
        else -> null
    }
}
