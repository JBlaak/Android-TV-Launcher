package dev.jblaak.tvlauncher.apps

import android.content.Intent
import android.os.Build
import dev.jblaak.tvlauncher.home.Rendering

/**
 * The emulator runs API 31, the TV API 30. Start a debug build with `--ez force_api30_rendering true` (or
 * `make run.debug.api30`) to see the TV's rendering path on the emulator.
 */
internal fun buildRendering(intent: Intent): Rendering = when {
    intent.getBooleanExtra("force_api30_rendering", false) -> Rendering.Api30
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> Rendering.Api31
    else -> Rendering.Api30
}
