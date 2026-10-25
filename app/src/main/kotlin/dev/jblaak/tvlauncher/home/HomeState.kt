package dev.jblaak.tvlauncher.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import dev.jblaak.tvlauncher.R
import dev.jblaak.tvlauncher.sky.SkyScene
import dev.jblaak.tvlauncher.sky.Weather
import dev.jblaak.tvlauncher.sky.Wind
import dev.jblaak.tvlauncher.weather.CurrentWeather
import dev.jblaak.tvlauncher.weather.WeatherFetch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * [focused] is the tile focus returns to, also while the gear has the focus.
 */
internal data class HomeState(
    val tiles: List<Tile>,
    val focused: App?,
    val gearFocused: Boolean,
    val time: LocalTime,
    val date: LocalDate,
    @param:StringRes val greeting: Int,
    val sky: SkyScene,
    val sunTime: SunTime?,
    val weather: PillWeather,
) {
    val focusedTile: App? get() = focused.takeUnless { gearFocused }

    val label: FocusLabel?
        get() = if (gearFocused) {
            FocusLabel(R.string.settings, R.string.settings_description)
        } else {
            tiles.firstOrNull { it.app == focused }?.let { FocusLabel(it.name, it.description) }
        }

    companion object {
        val previewValue: HomeState = preview(ZonedDateTime.of(2026, 10, 25, 17, 5, 0, 0, ZoneId.of("Europe/Amsterdam")), Weather.Clear)

        val nightPreviewValue: HomeState = preview(ZonedDateTime.of(2026, 10, 25, 23, 0, 0, 0, ZoneId.of("Europe/Amsterdam")), Weather.Clear)

        val rainPreviewValue: HomeState = preview(ZonedDateTime.of(2026, 10, 25, 17, 5, 0, 0, ZoneId.of("Europe/Amsterdam")), Weather.Rain)

        private fun preview(time: ZonedDateTime, weather: Weather): HomeState = HomeStateHolder(
            config = HomeConfig.Enschede,
            installedApps = object : InstalledApps {
                override fun isInstalled(packageName: String): Boolean = true

                override fun banner(packageName: String): AppBanner? = null
            },
            lastOpenedApp = object : LastOpenedApp {
                override fun get(): App? = null

                override fun set(app: App) = Unit
            },
            clock = object : Clock {
                override fun now(): ZonedDateTime = time
            },
            weatherSource = object : WeatherSource {
                override suspend fun fetch(): WeatherFetch = WeatherFetch.Failed
            },
            lastWeather = object : LastWeather {
                override fun get(): FetchedWeather = FetchedWeather(
                    current = CurrentWeather(weather = weather, temperature = 9.0, wind = Wind(from = 225.0, speed = 20.0, gusts = 35.0)),
                    at = time.toInstant(),
                )

                override fun set(weather: FetchedWeather) = Unit
            },
            listener = object : HomeStateHolder.Events {
                override fun appLaunchRequested(packageName: String) = Unit

                override fun settingsRequested() = Unit
            },
        ).state.value
    }
}

/**
 * The next sunrise or sunset for the pill: [label] is "Sunrise %s" or "Sunset %s", filled with [time].
 */
internal data class SunTime(
    @param:StringRes val label: Int,
    val time: LocalTime,
)

/**
 * The weather half of the pill: an [icon], the [condition] and the [temperature] in whole degrees, which is null
 * when there's no recent weather.
 */
internal data class PillWeather(
    @param:DrawableRes val icon: Int,
    @param:StringRes val condition: Int,
    val temperature: Int?,
)

internal data class FocusLabel(
    @param:StringRes val name: Int,
    @param:StringRes val description: Int,
)

internal data class Tile(
    val app: App,
    @param:StringRes val name: Int,
    @param:StringRes val description: Int,
    val logo: TileLogo,
    val colors: BrandColors,
)

internal sealed interface TileLogo {
    data class Mark(
        @param:DrawableRes val drawable: Int,
        val size: Dp,
        val colors: LogoColors?,
    ) : TileLogo

    data class Banner(val painter: Painter) : TileLogo

    data class Wordmark(
        @param:StringRes val name: Int,
        val size: Dp,
        val colors: LogoColors?,
    ) : TileLogo
}
