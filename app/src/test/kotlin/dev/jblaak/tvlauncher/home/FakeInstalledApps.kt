package dev.jblaak.tvlauncher.home

internal class FakeInstalledApps(
    installed: Set<String>,
    private val banners: Map<String, AppBanner> = emptyMap(),
) : InstalledApps {

    private val installed = installed.toMutableSet()

    override fun isInstalled(packageName: String): Boolean = packageName in installed

    override fun banner(packageName: String): AppBanner? = banners[packageName]

    fun install(packageName: String) {
        installed += packageName
    }
}
