package dev.jblaak.tvlauncher.home

import java.time.ZonedDateTime

internal class FakeClock(
    var time: ZonedDateTime,
) : Clock {

    override fun now(): ZonedDateTime = time
}
