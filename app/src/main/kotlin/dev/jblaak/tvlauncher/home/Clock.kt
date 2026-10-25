package dev.jblaak.tvlauncher.home

import java.time.ZonedDateTime

/**
 * The current time in the TV's own time zone.
 */
internal interface Clock {
    fun now(): ZonedDateTime
}
