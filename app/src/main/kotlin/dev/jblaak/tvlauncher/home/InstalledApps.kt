package dev.jblaak.tvlauncher.home

import androidx.compose.ui.graphics.painter.Painter

internal interface InstalledApps {
    fun isInstalled(packageName: String): Boolean

    fun banner(packageName: String): AppBanner?
}

internal data class AppBanner(
    val painter: Painter,
    val colors: BrandColors,
)
