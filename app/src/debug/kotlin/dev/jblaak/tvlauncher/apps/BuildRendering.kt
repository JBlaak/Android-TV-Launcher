package dev.jblaak.tvlauncher.apps

import android.content.Intent
import android.os.Build
import dev.jblaak.tvlauncher.home.Rendering

internal fun buildRendering(intent: Intent): Rendering = when {
    intent.getBooleanExtra("force_api30_rendering", false) -> Rendering.LowResolutionBlur
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> Rendering.RenderEffectBlur
    else -> Rendering.LowResolutionBlur
}
