package dev.jblaak.tvlauncher.apps

import dev.jblaak.tvlauncher.home.AppBanner
import dev.jblaak.tvlauncher.home.InstalledApps

/**
 * The emulator has none of the five apps, so debug builds report every app as installed. Release builds use
 * [PackageManagerInstalledApps] as is.
 */
internal fun buildInstalledApps(installedApps: PackageManagerInstalledApps): InstalledApps = object : InstalledApps {
    override fun isInstalled(packageName: String): Boolean = true

    override fun banner(packageName: String): AppBanner? = installedApps.banner(packageName)
}
