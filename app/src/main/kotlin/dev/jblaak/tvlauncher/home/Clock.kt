package dev.jblaak.tvlauncher.home

import java.time.ZonedDateTime

internal fun interface Clock {
    fun now(): ZonedDateTime
}
