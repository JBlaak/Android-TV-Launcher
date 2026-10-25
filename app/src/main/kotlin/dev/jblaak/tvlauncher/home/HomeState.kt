package dev.jblaak.tvlauncher.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import dev.jblaak.tvlauncher.R
import dev.jblaak.tvlauncher.sky.SkyScene
import dev.jblaak.tvlauncher.sky.SkyWeather
import dev.jblaak.tvlauncher.sky.Wind
import dev.jblaak.tvlauncher.sky.typicalWind
import dev.jblaak.tvlauncher.weather.CurrentWeather
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

internal data class HomeState(
    val tiles: List<Tile>,
    val focused: App?,
    val gearFocused: Boolean,
    val time: LocalTime,
    val date: LocalDate,
    val greeting: Greeting,
    val celebration: Celebration?,
    val sky: SkyScene?,
    val sunTime: SunTime?,
    val weather: PillWeather?,
) {
    val focusedTile: App? get() = focused.takeUnless { gearFocused }

    val label: FocusLabel?
        get() = if (gearFocused) {
            FocusLabel(R.string.settings, R.string.settings_description)
        } else {
            tiles.firstOrNull { it.app == focused }?.let { FocusLabel(it.name, it.description) }
        }

    companion object {
        val previewValue: HomeState = preview(amsterdam(2026, 10, 25, 17, 5), SkyWeather.Clear)

        val nightPreviewValue: HomeState = preview(amsterdam(2026, 10, 26, 3, 0), SkyWeather.Clear)

        val rainPreviewValue: HomeState = preview(amsterdam(2026, 10, 25, 17, 5), SkyWeather.Rain)

        val stormPreviewValue: HomeState = preview(amsterdam(2026, 10, 25, 17, 5), SkyWeather.Storm)

        val newYearsEvePreviewValue: HomeState = preview(amsterdam(2026, 12, 31, 23, 30), SkyWeather.Clear)

        val kingsDayPreviewValue: HomeState = preview(amsterdam(2027, 4, 27, 14, 0), SkyWeather.Clear)

        private fun preview(time: ZonedDateTime, weather: SkyWeather): HomeState {
            val tiles = HomeConfig.Enschede.apps.map { it.tile(target = it.targets.first(), banner = null) }
            val outside = outside(time, HomeConfig.Enschede.window, CurrentWeather(weather, temperature = 9.0, wind = typicalWind(weather)))
            return HomeState(
                tiles = tiles,
                focused = tiles.first().app,
                gearFocused = false,
                time = time.toLocalTime().truncatedTo(ChronoUnit.MINUTES),
                date = time.toLocalDate(),
                greeting = greeting(time),
                celebration = celebration(time),
                sky = outside.sky,
                sunTime = outside.sunTime,
                weather = outside.weather,
            )
        }

        private fun amsterdam(year: Int, month: Int, day: Int, hour: Int, minute: Int): ZonedDateTime = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZoneId.of("Europe/Amsterdam"))
    }
}

internal data class SunTime(
    @param:StringRes val label: Int,
    val time: LocalTime,
)

internal data class PillWeather(
    @param:DrawableRes val icon: Int,
    @param:StringRes val condition: Int,
    val temperature: Int?,
    val wind: PillWind?,
) {
    companion object {
        fun of(liveWeather: CurrentWeather?, sunUp: Boolean): PillWeather {
            val (icon, condition) = when (liveWeather?.weather ?: SkyWeather.Clear) {
                SkyWeather.Clear -> if (sunUp) R.drawable.ic_weather_sun to R.string.sunny else R.drawable.ic_weather_moon to R.string.clear
                SkyWeather.Warm -> if (sunUp) R.drawable.ic_weather_sun to R.string.sunny else R.drawable.ic_weather_moon to R.string.warm
                SkyWeather.Clouds -> R.drawable.ic_weather_cloudy to R.string.cloudy
                SkyWeather.Rain -> R.drawable.ic_weather_rain to R.string.rain
                SkyWeather.Storm -> R.drawable.ic_weather_storm to R.string.storm
                SkyWeather.Snow -> R.drawable.ic_weather_snow to R.string.snow
                SkyWeather.Fog -> R.drawable.ic_weather_fog to R.string.fog
            }
            return PillWeather(
                icon = icon,
                condition = condition,
                temperature = liveWeather?.temperature?.roundToInt(),
                wind = liveWeather?.wind?.let { PillWind.ofStrong(it) },
            )
        }
    }
}

internal data class PillWind(
    @param:StringRes val from: Int,
    val speed: Int,
) {
    companion object {
        fun ofStrong(wind: Wind): PillWind? = if (wind.speed >= 50) PillWind(compassPoint(wind.from), wind.speed.roundToInt()) else null

        private fun compassPoint(from: Double): Int = listOf(
            R.string.compass_north,
            R.string.compass_north_east,
            R.string.compass_east,
            R.string.compass_south_east,
            R.string.compass_south,
            R.string.compass_south_west,
            R.string.compass_west,
            R.string.compass_north_west,
        )[(from / 45).roundToInt().mod(8)]
    }
}

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

internal fun AppConfig.tile(target: AppTarget, banner: AppBanner?): Tile = Tile(
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
