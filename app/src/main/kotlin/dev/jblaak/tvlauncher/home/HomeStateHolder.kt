package dev.jblaak.tvlauncher.home

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class HomeStateHolder(
    private val config: HomeConfig,
    private val installedApps: InstalledApps,
    private val lastOpenedApp: LastOpenedApp,
    private val listener: Events,
) {

    private val mutableState = MutableStateFlow(homeState())
    val state: StateFlow<HomeState> = mutableState.asStateFlow()

    fun focus(app: App) {
        mutableState.update { it.copy(focused = app) }
    }

    fun open(app: App) {
        val target = config.apps.single { it.app == app }.installedTarget() ?: return
        lastOpenedApp.set(app)
        listener.appLaunchRequested(target.packageName)
    }

    fun returnHome() {
        mutableState.value = homeState()
    }

    private fun homeState(): HomeState {
        val tiles = config.apps.mapNotNull { it.tile() }
        val lastOpened = lastOpenedApp.get()
        val focused = tiles.firstOrNull { it.app == lastOpened } ?: tiles.firstOrNull()
        return HomeState(tiles = tiles, focused = focused?.app)
    }

    private fun AppConfig.tile(): Tile? {
        val target = installedTarget() ?: return null
        val banner = if (logo == null) installedApps.banner(target.packageName) else null
        return Tile(
            app = app,
            name = name,
            description = target.description,
            logo = when {
                logo != null -> TileLogo.Mark(logo, logoSize, logoColors)
                banner != null -> TileLogo.Banner(banner.painter)
                else -> TileLogo.Wordmark(name, logoSize, logoColors)
            },
            colors = banner?.colors ?: colors,
        )
    }

    private fun AppConfig.installedTarget(): AppTarget? = targets.firstOrNull { installedApps.isInstalled(it.packageName) }

    interface Events {
        fun appLaunchRequested(packageName: String)
    }
}
