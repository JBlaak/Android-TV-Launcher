package dev.jblaak.tvlauncher.home

import dev.jblaak.tvlauncher.weather.WeatherFetch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.time.Duration.Companion.minutes

internal class HomeStateHolder(
    private val config: HomeConfig,
    private val installedApps: InstalledApps,
    private val lastOpenedApp: LastOpenedApp,
    private val clock: Clock,
    private val weatherSource: WeatherSource,
    private val lastWeather: LastWeather,
    private val listener: Events,
) {

    private var fetchedWeather: FetchedWeather? = null

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

    suspend fun keepTime() {
        while (true) {
            delay(untilNextMinute(clock.now()))
            val now = clock.now()
            mutableState.update {
                it.copy(
                    time = now.minute(),
                    date = now.toLocalDate(),
                    greeting = greeting(now),
                    celebration = celebration(now),
                ).withOutside(now)
            }
        }
    }

    private fun untilNextMinute(now: ZonedDateTime): Long = now.until(now.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1), ChronoUnit.MILLIS)

    suspend fun keepWeather() {
        while (true) {
            if (fetchedWeather?.isOlderThan(Duration.ofMinutes(15)) != false) fetchWeather()
            delay(15.minutes)
        }
    }

    private suspend fun fetchWeather() {
        val fetch = weatherSource.fetch()
        if (fetch !is WeatherFetch.Fetched) return
        val now = clock.now()
        val fetched = FetchedWeather(fetch.current, now.toInstant())
        fetchedWeather = fetched
        lastWeather.set(fetched)
        mutableState.update { it.withOutside(now) }
    }

    private fun HomeState.withOutside(now: ZonedDateTime): HomeState {
        val outside = outsideOrPlainDock(now)
        return copy(sky = outside?.sky, sunTime = outside?.sunTime, weather = outside?.weather)
    }

    private fun outsideOrPlainDock(now: ZonedDateTime): Outside? = try {
        outside(now, config.window, liveWeather()?.current)
    } catch (failure: RuntimeException) {
        logger.log(Level.WARNING, "The sky or the weather failed, so Home shows the plain dock", failure)
        null
    }

    private fun liveWeather(): FetchedWeather? = lastFetchedWeather()?.takeUnless { it.isOlderThan(Duration.ofHours(3)) }

    private fun lastFetchedWeather(): FetchedWeather? = fetchedWeather ?: lastWeather.get().also { fetchedWeather = it }

    private fun FetchedWeather.isOlderThan(age: Duration): Boolean = Duration.between(at, clock.now().toInstant()) > age

    private fun homeState(): HomeState {
        val tiles = config.apps.mapNotNull { it.installedTile() }
        val lastOpened = lastOpenedApp.get()
        val focused = tiles.firstOrNull { it.app == lastOpened } ?: tiles.firstOrNull()
        val now = clock.now()
        val outside = outsideOrPlainDock(now)
        return HomeState(
            tiles = tiles,
            focused = focused?.app,
            gearFocused = false,
            time = now.minute(),
            date = now.toLocalDate(),
            greeting = greeting(now),
            celebration = celebration(now),
            sky = outside?.sky,
            sunTime = outside?.sunTime,
            weather = outside?.weather,
        )
    }

    private fun ZonedDateTime.minute() = toLocalTime().truncatedTo(ChronoUnit.MINUTES)

    private fun AppConfig.installedTile(): Tile? {
        val target = installedTarget() ?: return null
        return tile(target, banner = if (logo == null) installedApps.banner(target.packageName) else null)
    }

    private fun AppConfig.installedTarget(): AppTarget? = targets.firstOrNull { installedApps.isInstalled(it.packageName) }

    interface Events {
        fun appLaunchRequested(packageName: String)

        fun settingsRequested()
    }
}

private val logger = Logger.getLogger("Home")
