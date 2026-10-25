package dev.jblaak.tvlauncher.apps

import android.content.Intent
import dev.jblaak.tvlauncher.home.Clock
import dev.jblaak.tvlauncher.time.SystemClock
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime

/**
 * Debug builds can start at another local time to check the sky by eye, for example
 * `adb shell am start -n dev.jblaak.tvlauncher/.HomeActivity --es start_time 2026-10-25T17:05`. Time runs on from
 * there.
 */
internal fun buildClock(intent: Intent): Clock {
    val startTime = intent.getStringExtra("start_time") ?: return SystemClock()
    val now = ZonedDateTime.now()
    val offset = Duration.between(now, LocalDateTime.parse(startTime).atZone(now.zone))
    return object : Clock {
        override fun now(): ZonedDateTime = ZonedDateTime.now().plus(offset)
    }
}
