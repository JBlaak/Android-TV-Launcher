package dev.jblaak.tvlauncher.apps

import android.content.Intent
import android.os.Build
import dev.jblaak.tvlauncher.home.Rendering

@Suppress("UNUSED_PARAMETER")
internal fun buildRendering(intent: Intent): Rendering = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Rendering.RenderEffectBlur else Rendering.LowResolutionBlur
