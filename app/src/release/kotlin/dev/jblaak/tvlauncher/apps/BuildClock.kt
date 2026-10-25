package dev.jblaak.tvlauncher.apps

import android.content.Intent
import dev.jblaak.tvlauncher.home.Clock
import java.time.ZonedDateTime

@Suppress("UNUSED_PARAMETER")
internal fun buildClock(intent: Intent): Clock = Clock { ZonedDateTime.now() }
