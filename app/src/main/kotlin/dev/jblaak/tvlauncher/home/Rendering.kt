package dev.jblaak.tvlauncher.home

import androidx.compose.runtime.staticCompositionLocalOf

internal enum class Rendering {
    LowResolutionBlur,
    RenderEffectBlur,
}

internal val LocalRendering = staticCompositionLocalOf { Rendering.LowResolutionBlur }
