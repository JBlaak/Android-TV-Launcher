package dev.jblaak.tvlauncher.apps

import dev.jblaak.tvlauncher.home.AppBanner
import dev.jblaak.tvlauncher.home.InstalledApps

internal fun buildInstalledApps(installedApps: PackageManagerInstalledApps): InstalledApps = object : InstalledApps {
    override fun isInstalled(packageName: String): Boolean = true

    override fun banner(packageName: String): AppBanner? = installedApps.banner(packageName)
}
