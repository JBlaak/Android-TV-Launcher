package dev.jblaak.tvlauncher.apps

import android.content.Intent
import dev.jblaak.tvlauncher.home.Clock
import dev.jblaak.tvlauncher.time.SystemClock
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime

internal fun buildClock(intent: Intent): Clock {
    val startTime = intent.getStringExtra("start_time") ?: return SystemClock()
    val now = ZonedDateTime.now()
    val offset = Duration.between(now, LocalDateTime.parse(startTime).atZone(now.zone))
    return object : Clock {
        override fun now(): ZonedDateTime = ZonedDateTime.now().plus(offset)
    }
}
