package dev.jblaak.tvlauncher.time

import dev.jblaak.tvlauncher.home.Clock
import java.time.ZonedDateTime

internal class SystemClock : Clock {
    override fun now(): ZonedDateTime = ZonedDateTime.now()
}
