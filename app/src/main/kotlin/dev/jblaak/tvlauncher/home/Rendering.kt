package dev.jblaak.tvlauncher.home

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Which graphics Home may use. [Api31] allows `RenderEffect` (real blur); [Api30] is the path for the Android 11 TV,
 * without it. Previews and snapshots render the API 30 path.
 */
internal enum class Rendering {
    Api30,
    Api31,
}

internal val LocalRendering = staticCompositionLocalOf { Rendering.Api30 }
