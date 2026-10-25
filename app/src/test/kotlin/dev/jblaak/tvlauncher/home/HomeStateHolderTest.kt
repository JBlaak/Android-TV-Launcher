package dev.jblaak.tvlauncher.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.unit.dp
import dev.jblaak.tvlauncher.R
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeStateHolderTest {

    @Test
    fun `the dock shows the five apps in a fixed order`() {
        /* When */
        val holder = createHolder()

        /* Then */
        assertEquals(
            listOf(App.NpoTv, App.Netflix, App.DisneyPlus, App.Plex, App.YouTube),
            holder.state.value.tiles.map { it.app },
        )
    }

    @Test
    fun `an app that is not installed has no tile`() {
        /* Given */
        val installedApps = FakeInstalledApps(allPackages - "com.netflix.ninja")

        /* When */
        val holder = createHolder(installedApps = installedApps)

        /* Then */
        assertEquals(
            listOf(App.NpoTv, App.DisneyPlus, App.Plex, App.YouTube),
            holder.state.value.tiles.map { it.app },
        )
    }

    @Test
    fun `the plex tile stays when only the official plex app is installed`() {
        /* Given */
        val installedApps = FakeInstalledApps(allPackages - "com.edde746.plezy")

        /* When */
        val holder = createHolder(installedApps = installedApps)

        /* Then */
        assertEquals(App.Plex, holder.state.value.tiles[3].app)
    }

    @Test
    fun `the plex tile is hidden when neither plezy nor plex is installed`() {
        /* Given */
        val installedApps = FakeInstalledApps(allPackages - "com.edde746.plezy" - "com.plexapp.android")

        /* When */
        val holder = createHolder(installedApps = installedApps)

        /* Then */
        assertEquals(
            listOf(App.NpoTv, App.Netflix, App.DisneyPlus, App.YouTube),
            holder.state.value.tiles.map { it.app },
        )
    }

    @Test
    fun `the plex tile says it plays through plezy`() {
        /* When */
        val holder = createHolder()

        /* Then */
        assertEquals(R.string.your_library_via_plezy, holder.state.value.tile(App.Plex).description)
    }

    @Test
    fun `the plex tile drops the plezy mention when it falls back to the official app`() {
        /* Given */
        val installedApps = FakeInstalledApps(allPackages - "com.edde746.plezy")

        /* When */
        val holder = createHolder(installedApps = installedApps)

        /* Then */
        assertEquals(R.string.your_library, holder.state.value.tile(App.Plex).description)
    }

    @Test
    fun `opening netflix launches the netflix tv app`() {
        /* Given */
        val events = RecordingEvents()
        val holder = createHolder(listener = events)

        /* When */
        holder.open(App.Netflix)

        /* Then */
        assertEquals(listOf("com.netflix.ninja"), events.launchedPackages)
    }

    @Test
    fun `opening plex launches plezy`() {
        /* Given */
        val events = RecordingEvents()
        val holder = createHolder(listener = events)

        /* When */
        holder.open(App.Plex)

        /* Then */
        assertEquals(listOf("com.edde746.plezy"), events.launchedPackages)
    }

    @Test
    fun `opening plex launches the official plex app when plezy is missing`() {
        /* Given */
        val events = RecordingEvents()
        val installedApps = FakeInstalledApps(allPackages - "com.edde746.plezy")
        val holder = createHolder(installedApps = installedApps, listener = events)

        /* When */
        holder.open(App.Plex)

        /* Then */
        assertEquals(listOf("com.plexapp.android"), events.launchedPackages)
    }

    @Test
    fun `each app launches its own package`() {
        /* Given */
        val events = RecordingEvents()
        val holder = createHolder(listener = events)

        /* When */
        App.entries.forEach { holder.open(it) }

        /* Then */
        assertEquals(
            listOf(
                "tech.blaak.npotv",
                "com.netflix.ninja",
                "com.disney.disneyplus",
                "com.edde746.plezy",
                "com.google.android.youtube.tv",
            ),
            events.launchedPackages,
        )
    }

    @Test
    fun `opening an app remembers it as the last one opened`() {
        /* Given */
        val lastOpenedApp = FakeLastOpenedApp()
        val holder = createHolder(lastOpenedApp = lastOpenedApp)

        /* When */
        holder.open(App.YouTube)

        /* Then */
        assertEquals(App.YouTube, lastOpenedApp.app)
    }

    @Test
    fun `focus starts on npo tv when no app was opened before`() {
        /* When */
        val holder = createHolder()

        /* Then */
        assertEquals(App.NpoTv, holder.state.value.focused)
    }

    @Test
    fun `focus starts on the last app opened`() {
        /* Given */
        val lastOpenedApp = FakeLastOpenedApp(App.DisneyPlus)

        /* When */
        val holder = createHolder(lastOpenedApp = lastOpenedApp)

        /* Then */
        assertEquals(App.DisneyPlus, holder.state.value.focused)
    }

    @Test
    fun `focus falls back to npo tv when the last app opened is no longer installed`() {
        /* Given */
        val lastOpenedApp = FakeLastOpenedApp(App.Netflix)
        val installedApps = FakeInstalledApps(allPackages - "com.netflix.ninja")

        /* When */
        val holder = createHolder(installedApps = installedApps, lastOpenedApp = lastOpenedApp)

        /* Then */
        assertEquals(App.NpoTv, holder.state.value.focused)
    }

    @Test
    fun `focusing a tile moves the focus to it`() {
        /* Given */
        val holder = createHolder()

        /* When */
        holder.focus(App.Plex)

        /* Then */
        assertEquals(App.Plex, holder.state.value.focused)
    }

    @Test
    fun `returning home puts the focus back on the last app opened`() {
        /* Given */
        val holder = createHolder()
        holder.open(App.Netflix)
        holder.focus(App.YouTube)

        /* When */
        holder.returnHome()

        /* Then */
        assertEquals(App.Netflix, holder.state.value.focused)
    }

    @Test
    fun `an app installed while away gets its tile on return home`() {
        /* Given */
        val installedApps = FakeInstalledApps(allPackages - "com.netflix.ninja")
        val holder = createHolder(installedApps = installedApps)

        /* When */
        installedApps.install("com.netflix.ninja")
        holder.returnHome()

        /* Then */
        assertEquals(App.Netflix, holder.state.value.tiles[1].app)
    }

    @Test
    fun `an app with a bundled logo shows that logo`() {
        /* When */
        val holder = createHolder()

        /* Then */
        assertEquals(
            TileLogo.Mark(
                drawable = R.drawable.logo_netflix,
                size = 37.5.dp,
                colors = LogoColors(idle = Color(0xFFE50914), focused = Color.White),
            ),
            holder.state.value.tile(App.Netflix).logo,
        )
    }

    @Test
    fun `disney plus shows its own tv banner`() {
        /* Given */
        val banner = AppBanner(ColorPainter(Color.Blue), BrandColors(listOf(Color.Cyan, Color.Blue), Color.Blue))
        val installedApps = FakeInstalledApps(allPackages, banners = mapOf("com.disney.disneyplus" to banner))

        /* When */
        val holder = createHolder(installedApps = installedApps)

        /* Then */
        assertEquals(TileLogo.Banner(banner.painter), holder.state.value.tile(App.DisneyPlus).logo)
    }

    @Test
    fun `disney plus takes its tile colours from its banner`() {
        /* Given */
        val banner = AppBanner(ColorPainter(Color.Blue), BrandColors(listOf(Color.Cyan, Color.Blue), Color.Blue))
        val installedApps = FakeInstalledApps(allPackages, banners = mapOf("com.disney.disneyplus" to banner))

        /* When */
        val holder = createHolder(installedApps = installedApps)

        /* Then */
        assertEquals(BrandColors(listOf(Color.Cyan, Color.Blue), Color.Blue), holder.state.value.tile(App.DisneyPlus).colors)
    }

    @Test
    fun `disney plus shows its name when the system has no banner for it`() {
        /* When */
        val holder = createHolder()

        /* Then */
        assertEquals(
            TileLogo.Wordmark(
                name = R.string.disney_plus,
                size = 21.dp,
                colors = LogoColors(idle = Color(0xFFE8ECFF), focused = Color.White),
            ),
            holder.state.value.tile(App.DisneyPlus).logo,
        )
    }

    private fun HomeState.tile(app: App): Tile = tiles.single { it.app == app }

    private fun createHolder(
        installedApps: InstalledApps = FakeInstalledApps(allPackages),
        lastOpenedApp: LastOpenedApp = FakeLastOpenedApp(),
        listener: HomeStateHolder.Events = RecordingEvents(),
    ): HomeStateHolder = HomeStateHolder(
        config = HomeConfig.Enschede,
        installedApps = installedApps,
        lastOpenedApp = lastOpenedApp,
        listener = listener,
    )

    private companion object {
        val allPackages = setOf(
            "tech.blaak.npotv",
            "com.netflix.ninja",
            "com.disney.disneyplus",
            "com.edde746.plezy",
            "com.plexapp.android",
            "com.google.android.youtube.tv",
        )
    }
}
