package dev.jblaak.tvlauncher.home

import dev.jblaak.tvlauncher.R
import dev.jblaak.tvlauncher.sky.SkyScene
import dev.jblaak.tvlauncher.sky.Sun
import dev.jblaak.tvlauncher.sky.SunEvent
import dev.jblaak.tvlauncher.sky.Weather
import dev.jblaak.tvlauncher.sky.nextSunEvent
import dev.jblaak.tvlauncher.sky.skyScene
import dev.jblaak.tvlauncher.weather.WeatherFetch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
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

    private var fetchedWeather: FetchedWeather? = lastWeather.get()

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
                    sunTime = time.sunTime(),
                ).withWeather(time)
            }
        }
    }

    /**
     * Fetches the weather when the last fetch is more than 15 minutes old, and checks again every 15 minutes, until
     * cancelled. A failed fetch changes nothing.
     */
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
        mutableState.update { it.withWeather(now) }
    }

    private fun HomeState.withWeather(now: ZonedDateTime): HomeState {
        val sky = skyScene(now.toInstant(), config.window, liveWeather()?.current?.weather ?: Weather.Clear)
        return copy(sky = sky, weather = pillWeather(sky))
    }

    /**
     * The weather last fetched, while it's at most 3 hours old.
     */
    private fun liveWeather(): FetchedWeather? = fetchedWeather?.takeUnless { it.isOlderThan(Duration.ofHours(3)) }

    private fun FetchedWeather.isOlderThan(age: Duration): Boolean = Duration.between(at, clock.now().toInstant()) > age

    private fun pillWeather(sky: SkyScene): PillWeather {
        val current = liveWeather()?.current
        val sunUp = sky.sun != Sun.BelowHorizon
        val (icon, condition) = when (current?.weather ?: Weather.Clear) {
            Weather.Clear -> if (sunUp) R.drawable.ic_weather_sun to R.string.sunny else R.drawable.ic_weather_moon to R.string.clear
            Weather.Warm -> if (sunUp) R.drawable.ic_weather_sun to R.string.sunny else R.drawable.ic_weather_moon to R.string.warm
            Weather.Cloudy -> R.drawable.ic_weather_cloudy to R.string.cloudy
            Weather.Rain -> R.drawable.ic_weather_rain to R.string.rain
            Weather.Storm -> R.drawable.ic_weather_storm to R.string.storm
            Weather.Snow -> R.drawable.ic_weather_snow to R.string.snow
            Weather.Fog -> R.drawable.ic_weather_fog to R.string.fog
        }
        return PillWeather(icon = icon, condition = condition, temperature = current?.temperature?.roundToInt())
    }

    private fun homeState(): HomeState {
        val tiles = config.apps.mapNotNull { it.tile() }
        val lastOpened = lastOpenedApp.get()
        val focused = tiles.firstOrNull { it.app == lastOpened } ?: tiles.firstOrNull()
        val now = clock.now()
        val sky = skyScene(now.toInstant(), config.window, liveWeather()?.current?.weather ?: Weather.Clear)
        return HomeState(
            tiles = tiles,
            focused = focused?.app,
            gearFocused = false,
            time = now.minute(),
            date = now.toLocalDate(),
            greeting = now.greeting(),
            sky = sky,
            sunTime = now.sunTime(),
            weather = pillWeather(sky),
        )
    }

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
