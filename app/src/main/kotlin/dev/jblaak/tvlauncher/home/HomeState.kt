package dev.jblaak.tvlauncher.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import dev.jblaak.tvlauncher.sky.SkyScene
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal data class HomeState(
    val tiles: List<Tile>,
    val focused: App?,
    val sky: SkyScene,
    val sunTime: SunTime?,
) {
    companion object {
        val previewValue: HomeState = preview(ZonedDateTime.of(2026, 10, 25, 17, 5, 0, 0, ZoneId.of("Europe/Amsterdam")))

        val nightPreviewValue: HomeState = preview(ZonedDateTime.of(2026, 10, 25, 23, 0, 0, 0, ZoneId.of("Europe/Amsterdam")))

        private fun preview(time: ZonedDateTime): HomeState = HomeStateHolder(
            config = HomeConfig.Enschede,
            clock = { time },
            installedApps = object : InstalledApps {
                override fun isInstalled(packageName: String): Boolean = true

                override fun banner(packageName: String): AppBanner? = null
            },
            lastOpenedApp = object : LastOpenedApp {
                override fun get(): App? = null

                override fun set(app: App) = Unit
            },
            listener = object : HomeStateHolder.Events {
                override fun appLaunchRequested(packageName: String) = Unit
            },
        ).state.value
    }
}

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

/**
 * The next sunrise or sunset for the pill: [label] is "Sunrise %s" or "Sunset %s", filled with [time].
 */
internal data class SunTime(
    @param:StringRes val label: Int,
    val time: LocalTime,
)
