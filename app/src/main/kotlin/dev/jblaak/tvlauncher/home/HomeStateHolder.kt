package dev.jblaak.tvlauncher.home

import dev.jblaak.tvlauncher.R
import dev.jblaak.tvlauncher.sky.SkyScene
import dev.jblaak.tvlauncher.sky.SunEvent
import dev.jblaak.tvlauncher.sky.WeatherType
import dev.jblaak.tvlauncher.sky.nextSunEvent
import dev.jblaak.tvlauncher.sky.skyScene
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

internal class HomeStateHolder(
    private val config: HomeConfig,
    private val installedApps: InstalledApps,
    private val lastOpenedApp: LastOpenedApp,
    private val clock: Clock,
    private val listener: Events,
) {

    private val mutableState = MutableStateFlow(homeState())
    val state: StateFlow<HomeState> = mutableState.asStateFlow()

    fun focus(app: App) {
        mutableState.update { it.copy(focused = app, gearFocused = false) }
    }

    fun focusSettings() {
        mutableState.update { it.copy(gearFocused = true) }
    }

    fun leaveSettings() {
        mutableState.update { it.copy(gearFocused = false) }
    }

    fun open(app: App) {
        val target = config.apps.single { it.app == app }.installedTarget() ?: return
        lastOpenedApp.set(app)
        listener.appLaunchRequested(target.packageName)
    }

    fun openSettings() {
        listener.settingsRequested()
    }

    fun returnHome() {
        mutableState.value = homeState()
    }

    /**
     * Moves the clock, the sky and the sun time on at the start of every minute, until cancelled.
     */
    suspend fun keepTime() {
        while (true) {
            val now = clock.now()
            delay(now.until(now.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1), ChronoUnit.MILLIS))
            val time = clock.now()
            mutableState.update {
                it.copy(
                    time = time.minute(),
                    date = time.toLocalDate(),
                    greeting = time.greeting(),
                    sky = time.sky(),
                    sunTime = time.sunTime(),
                )
            }
        }
    }

    private fun homeState(): HomeState {
        val tiles = config.apps.mapNotNull { it.tile() }
        val lastOpened = lastOpenedApp.get()
        val focused = tiles.firstOrNull { it.app == lastOpened } ?: tiles.firstOrNull()
        val now = clock.now()
        return HomeState(
            tiles = tiles,
            focused = focused?.app,
            gearFocused = false,
            time = now.minute(),
            date = now.toLocalDate(),
            greeting = now.greeting(),
            sky = now.sky(),
            sunTime = now.sunTime(),
        )
    }

    private fun ZonedDateTime.sky(): SkyScene = skyScene(toInstant(), config.window, WeatherType.Clear)

    private fun ZonedDateTime.sunTime(): SunTime? = when (val event = nextSunEvent(this, config.window.location)) {
        is SunEvent.Sunrise -> SunTime(R.string.sunrise_time, event.at.toLocalTime())
        is SunEvent.Sunset -> SunTime(R.string.sunset_time, event.at.toLocalTime())
        null -> null
    }

    private fun ZonedDateTime.minute() = toLocalTime().truncatedTo(ChronoUnit.MINUTES)

    private fun ZonedDateTime.greeting(): Int = when (hour) {
        in 0..5 -> R.string.good_night
        in 6..11 -> R.string.good_morning
        in 12..17 -> R.string.good_afternoon
        else -> R.string.good_evening
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

        fun settingsRequested()
    }
}
