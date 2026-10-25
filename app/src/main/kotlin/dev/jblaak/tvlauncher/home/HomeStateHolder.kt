package dev.jblaak.tvlauncher.home

import dev.jblaak.tvlauncher.R
import dev.jblaak.tvlauncher.sky.SunEvent
import dev.jblaak.tvlauncher.sky.nextSunEvent
import dev.jblaak.tvlauncher.sky.skyScene
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.ZonedDateTime

internal class HomeStateHolder(
    private val config: HomeConfig,
    private val clock: Clock,
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

    fun timePassed() {
        val now = clock.now()
        mutableState.update { it.copy(sky = skyScene(now.toInstant(), config.window), sunTime = sunTime(now)) }
    }

    private fun homeState(): HomeState {
        val now = clock.now()
        val tiles = config.apps.mapNotNull { it.tile() }
        val lastOpened = lastOpenedApp.get()
        val focused = tiles.firstOrNull { it.app == lastOpened } ?: tiles.firstOrNull()
        return HomeState(
            tiles = tiles,
            focused = focused?.app,
            sky = skyScene(now.toInstant(), config.window),
            sunTime = sunTime(now),
        )
    }

    private fun sunTime(now: ZonedDateTime): SunTime? = when (val event = nextSunEvent(now, config.window.location)) {
        is SunEvent.Sunrise -> SunTime(R.string.sunrise_time, event.at.toLocalTime())
        is SunEvent.Sunset -> SunTime(R.string.sunset_time, event.at.toLocalTime())
        null -> null
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
