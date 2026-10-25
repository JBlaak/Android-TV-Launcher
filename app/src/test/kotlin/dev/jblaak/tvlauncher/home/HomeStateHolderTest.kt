package dev.jblaak.tvlauncher.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.unit.dp
import dev.jblaak.tvlauncher.R
import dev.jblaak.tvlauncher.sky.Sun
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
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
    fun `the clock shows the current time to the minute`() {
        /* When */
        val holder = createHolder(clock = FakeClock(at(21, 15, second = 42)))

        /* Then */
        assertEquals(LocalTime.of(21, 15), holder.state.value.time)
    }

    @Test
    fun `the greeting is followed by the date`() {
        /* When */
        val holder = createHolder(clock = FakeClock(at(21, 15)))

        /* Then */
        assertEquals(LocalDate.of(2026, 10, 25), holder.state.value.date)
    }

    @Test
    fun `the clock follows the time zone the tv is set to`() {
        /* Given */
        val clock = FakeClock(ZonedDateTime.of(2026, 10, 25, 23, 30, 0, 0, ZoneId.of("Europe/London")))

        /* When */
        val holder = createHolder(clock = clock)

        /* Then */
        assertEquals(LocalTime.of(23, 30), holder.state.value.time)
    }

    @Test
    fun `just after midnight it says good night`() {
        /* When */
        val holder = createHolder(clock = FakeClock(at(0, 0)))

        /* Then */
        assertEquals(R.string.good_night, holder.state.value.greeting)
    }

    @Test
    fun `just before six in the morning it still says good night`() {
        /* When */
        val holder = createHolder(clock = FakeClock(at(5, 59)))

        /* Then */
        assertEquals(R.string.good_night, holder.state.value.greeting)
    }

    @Test
    fun `from six in the morning it says good morning`() {
        /* When */
        val holder = createHolder(clock = FakeClock(at(6, 0)))

        /* Then */
        assertEquals(R.string.good_morning, holder.state.value.greeting)
    }

    @Test
    fun `just before noon it still says good morning`() {
        /* When */
        val holder = createHolder(clock = FakeClock(at(11, 59)))

        /* Then */
        assertEquals(R.string.good_morning, holder.state.value.greeting)
    }

    @Test
    fun `from noon it says good afternoon`() {
        /* When */
        val holder = createHolder(clock = FakeClock(at(12, 0)))

        /* Then */
        assertEquals(R.string.good_afternoon, holder.state.value.greeting)
    }

    @Test
    fun `just before six in the evening it still says good afternoon`() {
        /* When */
        val holder = createHolder(clock = FakeClock(at(17, 59)))

        /* Then */
        assertEquals(R.string.good_afternoon, holder.state.value.greeting)
    }

    @Test
    fun `from six in the evening it says good evening`() {
        /* When */
        val holder = createHolder(clock = FakeClock(at(18, 0)))

        /* Then */
        assertEquals(R.string.good_evening, holder.state.value.greeting)
    }

    @Test
    fun `just before midnight it still says good evening`() {
        /* When */
        val holder = createHolder(clock = FakeClock(at(23, 59)))

        /* Then */
        assertEquals(R.string.good_evening, holder.state.value.greeting)
    }

    @Test
    fun `the clock moves on when the next minute starts`() = runTest {
        /* Given */
        val clock = FakeClock(at(11, 59, second = 30))
        val holder = createHolder(clock = clock)
        backgroundScope.launch { holder.keepTime() }
        runCurrent()

        /* When */
        clock.time = at(12, 0)
        advanceTimeBy(30.seconds)
        runCurrent()

        /* Then */
        assertEquals(LocalTime.of(12, 0), holder.state.value.time)
    }

    @Test
    fun `the clock waits for the next minute before it moves on`() = runTest {
        /* Given */
        val clock = FakeClock(at(11, 59, second = 30))
        val holder = createHolder(clock = clock)
        backgroundScope.launch { holder.keepTime() }
        runCurrent()

        /* When */
        clock.time = at(11, 59, second = 59)
        advanceTimeBy(29.seconds)
        runCurrent()

        /* Then */
        assertEquals(LocalTime.of(11, 59), holder.state.value.time)
    }

    @Test
    fun `the greeting changes when the clock passes noon`() = runTest {
        /* Given */
        val clock = FakeClock(at(11, 59, second = 30))
        val holder = createHolder(clock = clock)
        backgroundScope.launch { holder.keepTime() }
        runCurrent()

        /* When */
        clock.time = at(12, 0)
        advanceTimeBy(30.seconds)
        runCurrent()

        /* Then */
        assertEquals(R.string.good_afternoon, holder.state.value.greeting)
    }

    @Test
    fun `returning home shows the current time straight away`() {
        /* Given */
        val clock = FakeClock(at(21, 15))
        val holder = createHolder(clock = clock)

        /* When */
        clock.time = at(23, 40)
        holder.returnHome()

        /* Then */
        assertEquals(LocalTime.of(23, 40), holder.state.value.time)
    }

    @Test
    fun `the label row shows the focused app`() {
        /* When */
        val holder = createHolder()

        /* Then */
        assertEquals(FocusLabel(R.string.npo_tv, R.string.npo_live_channels), holder.state.value.label)
    }

    @Test
    fun `the gear is not focused at first`() {
        /* When */
        val holder = createHolder()

        /* Then */
        assertEquals(false, holder.state.value.gearFocused)
    }

    @Test
    fun `focusing the gear focuses it`() {
        /* Given */
        val holder = createHolder()

        /* When */
        holder.focusSettings()

        /* Then */
        assertEquals(true, holder.state.value.gearFocused)
    }

    @Test
    fun `with the gear focused the label row describes the settings`() {
        /* Given */
        val holder = createHolder()

        /* When */
        holder.focusSettings()

        /* Then */
        assertEquals(FocusLabel(R.string.settings, R.string.settings_description), holder.state.value.label)
    }

    @Test
    fun `with the gear focused no tile is focused`() {
        /* Given */
        val holder = createHolder()

        /* When */
        holder.focusSettings()

        /* Then */
        assertEquals(null, holder.state.value.focusedTile)
    }

    @Test
    fun `leaving the gear returns to the tile focused before`() {
        /* Given */
        val holder = createHolder()
        holder.focus(App.Plex)
        holder.focusSettings()

        /* When */
        holder.leaveSettings()

        /* Then */
        assertEquals(App.Plex, holder.state.value.focusedTile)
    }

    @Test
    fun `leaving the gear unfocuses it`() {
        /* Given */
        val holder = createHolder()
        holder.focusSettings()

        /* When */
        holder.leaveSettings()

        /* Then */
        assertEquals(false, holder.state.value.gearFocused)
    }

    @Test
    fun `focusing a tile unfocuses the gear`() {
        /* Given */
        val holder = createHolder()
        holder.focusSettings()

        /* When */
        holder.focus(App.Netflix)

        /* Then */
        assertEquals(false, holder.state.value.gearFocused)
    }

    @Test
    fun `returning home unfocuses the gear`() {
        /* Given */
        val holder = createHolder()
        holder.focusSettings()

        /* When */
        holder.returnHome()

        /* Then */
        assertEquals(false, holder.state.value.gearFocused)
    }

    @Test
    fun `opening the settings asks for the tv settings`() {
        /* Given */
        val events = RecordingEvents()
        val holder = createHolder(listener = events)

        /* When */
        holder.openSettings()

        /* Then */
        assertEquals(1, events.settingsRequests)
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
    fun `the sun comes into view as the minutes pass`() = runTest {
        /* Given */
        val clock = FakeClock(amsterdam(2026, 10, 25, 12, 59, second = 30))
        val holder = createHolder(clock = clock)
        backgroundScope.launch { holder.keepTime() }
        runCurrent()

        /* When */
        clock.time = amsterdam(2026, 10, 25, 17, 5)
        advanceTimeBy(30.seconds)
        runCurrent()

        /* Then */
        assertTrue(holder.state.value.sky.sun is Sun.Disc)
    }

    @Test
    fun `the pill moves on to the sunset once the sun is up`() = runTest {
        /* Given */
        val clock = FakeClock(amsterdam(2026, 10, 25, 7, 29, second = 30))
        val holder = createHolder(clock = clock)
        backgroundScope.launch { holder.keepTime() }
        runCurrent()

        /* When */
        clock.time = amsterdam(2026, 10, 25, 7, 30)
        advanceTimeBy(30.seconds)
        runCurrent()

        /* Then */
        assertEquals(R.string.sunset_time, holder.state.value.sunTime?.label)
    }

    private fun HomeState.sunDisc(): Sun.Disc = sky.sun as Sun.Disc

    private fun assertAbout(expected: LocalTime, actual: LocalTime?) {
        val difference = Duration.between(expected, checkNotNull(actual)).abs()
        assertTrue("expected $expected ±2 min, was $actual", difference <= Duration.ofMinutes(2))
    }

    private fun amsterdam(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int = 0): ZonedDateTime = ZonedDateTime.of(year, month, day, hour, minute, second, 0, ZoneId.of("Europe/Amsterdam"))

    private fun HomeState.tile(app: App): Tile = tiles.single { it.app == app }

    private fun at(hour: Int, minute: Int, second: Int = 0): ZonedDateTime = ZonedDateTime.of(2026, 10, 25, hour, minute, second, 0, ZoneId.of("Europe/Amsterdam"))

    private fun createHolder(
        installedApps: InstalledApps = FakeInstalledApps(allPackages),
        lastOpenedApp: LastOpenedApp = FakeLastOpenedApp(),
        clock: Clock = FakeClock(at(21, 15)),
        listener: HomeStateHolder.Events = RecordingEvents(),
    ): HomeStateHolder = HomeStateHolder(
        config = HomeConfig.Enschede,
        installedApps = installedApps,
        lastOpenedApp = lastOpenedApp,
        clock = clock,
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
