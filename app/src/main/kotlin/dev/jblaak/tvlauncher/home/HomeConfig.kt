package dev.jblaak.tvlauncher.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.jblaak.tvlauncher.R
import dev.jblaak.tvlauncher.sky.Location
import dev.jblaak.tvlauncher.sky.Window

internal enum class App {
    NpoTv,
    Netflix,
    DisneyPlus,
    Plex,
    YouTube,
}

/**
 * [window] is the TV seen as a window onto the sky outside: where it hangs and which way it looks.
 */
internal data class HomeConfig(
    val window: Window,
    val apps: List<AppConfig>,
) {
    companion object {
        val Enschede = HomeConfig(
            window = Window(location = Location(latitude = 52.2215, longitude = 6.8937), facing = 270.0),
            apps = listOf(
                AppConfig(
                    app = App.NpoTv,
                    name = R.string.npo_tv,
                    targets = listOf(AppTarget("tech.blaak.npotv", R.string.npo_live_channels)),
                    logo = R.drawable.logo_npo_tv,
                    logoSize = 45.dp,
                    colors = BrandColors(listOf(Color(0xFF2B3A2C), Color(0xFF3E8E3A)), glow = Color(0xFF4AA83F)),
                    logoColors = null,
                ),
                AppConfig(
                    app = App.Netflix,
                    name = R.string.netflix,
                    targets = listOf(AppTarget("com.netflix.ninja", R.string.series_and_films)),
                    logo = R.drawable.logo_netflix,
                    logoSize = 37.5.dp,
                    colors = BrandColors(listOf(Color(0xFFFF3B30), Color(0xFFB20710)), glow = Color(0xFFE50914)),
                    logoColors = LogoColors(idle = Color(0xFFE50914), focused = Color.White),
                ),
                AppConfig(
                    app = App.DisneyPlus,
                    name = R.string.disney_plus,
                    targets = listOf(AppTarget("com.disney.disneyplus", R.string.series_and_films)),
                    logo = null,
                    logoSize = 21.dp,
                    colors = BrandColors(listOf(Color(0xFF1CC6D8), Color(0xFF1F6BFF), Color(0xFF170B6B)), glow = Color(0xFF1F6BFF)),
                    logoColors = LogoColors(idle = Color(0xFFE8ECFF), focused = Color.White),
                ),
                AppConfig(
                    app = App.Plex,
                    name = R.string.plex,
                    targets = listOf(
                        AppTarget("com.edde746.plezy", R.string.your_library_via_plezy),
                        AppTarget("com.plexapp.android", R.string.your_library),
                    ),
                    logo = R.drawable.logo_plex,
                    logoSize = 69.dp,
                    colors = BrandColors(listOf(Color(0xFFFFD36B), Color(0xFFE5A00D)), glow = Color(0xFFE5A00D)),
                    logoColors = LogoColors(idle = Color(0xFFEDEDED), focused = Color(0xFF1A1300)),
                ),
                AppConfig(
                    app = App.YouTube,
                    name = R.string.youtube,
                    targets = listOf(AppTarget("com.google.android.youtube.tv", R.string.subscriptions)),
                    logo = R.drawable.logo_youtube,
                    logoSize = 43.5.dp,
                    colors = BrandColors(listOf(Color(0xFFFF5F6D), Color(0xFFE0002D)), glow = Color(0xFFFF0033)),
                    logoColors = LogoColors(idle = Color(0xFFFF0033), focused = Color.White),
                ),
            ),
        )
    }
}

/**
 * One app in the dock. [targets] are the packages that can open it, in order of preference.
 * Without a bundled [logo] the tile shows the app's TV banner from the system. Without [logoColors] the logo keeps
 * its own colours. [logoSize] is the logo's height, or the text size of the name when there's no logo.
 */
internal data class AppConfig(
    val app: App,
    @param:StringRes val name: Int,
    val targets: List<AppTarget>,
    @param:DrawableRes val logo: Int?,
    val logoSize: Dp,
    val colors: BrandColors,
    val logoColors: LogoColors?,
)

internal data class AppTarget(
    val packageName: String,
    @param:StringRes val description: Int,
)

internal data class BrandColors(
    val gradient: List<Color>,
    val glow: Color,
)

internal data class LogoColors(
    val idle: Color,
    val focused: Color,
)
