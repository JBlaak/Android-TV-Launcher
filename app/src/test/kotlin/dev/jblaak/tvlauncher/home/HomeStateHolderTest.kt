package dev.jblaak.tvlauncher.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.unit.dp
import dev.jblaak.tvlauncher.R
import dev.jblaak.tvlauncher.sky.Sun
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

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

    @Test
    fun `before dawn the pill shows the sunrise`() {
        /* When */
        val holder = createHolder(clock = FakeClock(amsterdam(2026, 10, 25, 5, 0)))

        /* Then */
        assertEquals(R.string.sunrise_time, holder.state.value.sunTime?.label)
    }

    @Test
    fun `on 25 october the sun rises at 07 16`() {
        /* When */
        val holder = createHolder(clock = FakeClock(amsterdam(2026, 10, 25, 5, 0)))

        /* Then */
        assertAbout(LocalTime.of(7, 16), holder.state.value.sunTime?.time)
    }

    @Test
    fun `during the day the pill shows the sunset`() {
        /* When */
        val holder = createHolder(clock = FakeClock(amsterdam(2026, 10, 25, 12, 0)))

        /* Then */
        assertEquals(R.string.sunset_time, holder.state.value.sunTime?.label)
    }

    @Test
    fun `on 25 october the sun sets at 17 18`() {
        /* When */
        val holder = createHolder(clock = FakeClock(amsterdam(2026, 10, 25, 12, 0)))

        /* Then */
        assertAbout(LocalTime.of(17, 18), holder.state.value.sunTime?.time)
    }

    @Test
    fun `after sunset the pill shows tomorrow's sunrise`() {
        /* When */
        val holder = createHolder(clock = FakeClock(amsterdam(2026, 10, 25, 21, 0)))

        /* Then */
        assertEquals(R.string.sunrise_time, holder.state.value.sunTime?.label)
    }

    @Test
    fun `at 17 05 on 25 october the sun stands left of centre`() {
        /* When */
        val holder = createHolder(clock = FakeClock(amsterdam(2026, 10, 25, 17, 5)))

        /* Then */
        assertTrue(holder.state.value.sunDisc().position.x < 640)
    }

    @Test
    fun `at 17 05 on 25 october the sun stands low`() {
        /* When */
        val holder = createHolder(clock = FakeClock(amsterdam(2026, 10, 25, 17, 5)))

        /* Then */
        assertTrue(holder.state.value.sunDisc().position.y in 560.0..600.0)
    }

    @Test
    fun `at 09 00 the sun is behind the viewer, so there is no disc`() {
        /* When */
        val holder = createHolder(clock = FakeClock(amsterdam(2026, 10, 25, 9, 0)))

        /* Then */
        assertEquals(Sun.BehindViewer, holder.state.value.sky.sun)
    }

    @Test
    fun `at midsummer the sun sets right of centre`() {
        /* When */
        val holder = createHolder(clock = FakeClock(amsterdam(2026, 6, 21, 21, 45)))

        /* Then */
        assertTrue(holder.state.value.sunDisc().position.x > 640)
    }

    @Test
    fun `the sun moves on as time passes`() {
        /* Given */
        val clock = FakeClock(amsterdam(2026, 10, 25, 9, 0))
        val holder = createHolder(clock = clock)

        /* When */
        clock.time = amsterdam(2026, 10, 25, 17, 5)
        holder.timePassed()

        /* Then */
        assertTrue(holder.state.value.sky.sun is Sun.Disc)
    }

    @Test
    fun `the pill moves on to the sunset once the sun is up`() {
        /* Given */
        val clock = FakeClock(amsterdam(2026, 10, 25, 7, 0))
        val holder = createHolder(clock = clock)

        /* When */
        clock.time = amsterdam(2026, 10, 25, 7, 30)
        holder.timePassed()

        /* Then */
        assertEquals(R.string.sunset_time, holder.state.value.sunTime?.label)
    }

    @Test
    fun `time passing keeps the focus where it is`() {
        /* Given */
        val holder = createHolder()
        holder.focus(App.Plex)

        /* When */
        holder.timePassed()

        /* Then */
        assertEquals(App.Plex, holder.state.value.focused)
    }

    private fun HomeState.sunDisc(): Sun.Disc = sky.sun as Sun.Disc

    private fun assertAbout(expected: LocalTime, actual: LocalTime?) {
        val difference = Duration.between(expected, checkNotNull(actual)).abs()
        assertTrue("expected $expected ±2 min, was $actual", difference <= Duration.ofMinutes(2))
    }

    private fun amsterdam(year: Int, month: Int, day: Int, hour: Int, minute: Int): ZonedDateTime = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZoneId.of("Europe/Amsterdam"))

    private fun HomeState.tile(app: App): Tile = tiles.single { it.app == app }

    private fun createHolder(
        installedApps: InstalledApps = FakeInstalledApps(allPackages),
        lastOpenedApp: LastOpenedApp = FakeLastOpenedApp(),
        listener: HomeStateHolder.Events = RecordingEvents(),
        clock: Clock = FakeClock(amsterdam(2026, 10, 25, 17, 5)),
    ): HomeStateHolder = HomeStateHolder(
        config = HomeConfig.Enschede,
        clock = clock,
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
