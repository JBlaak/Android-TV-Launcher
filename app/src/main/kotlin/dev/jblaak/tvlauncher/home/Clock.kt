package dev.jblaak.tvlauncher.home

import java.time.ZonedDateTime

internal interface Clock {
    fun now(): ZonedDateTime
}
