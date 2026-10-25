package dev.jblaak.tvlauncher.apps

import android.content.Intent
import dev.jblaak.tvlauncher.home.Clock
import dev.jblaak.tvlauncher.time.SystemClock

@Suppress("UNUSED_PARAMETER")
internal fun buildClock(intent: Intent): Clock = SystemClock()
