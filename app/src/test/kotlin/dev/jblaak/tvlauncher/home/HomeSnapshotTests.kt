package dev.jblaak.tvlauncher.home

import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import dev.jblaak.tvlauncher.Locale
import dev.jblaak.tvlauncher.Television
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
internal class HomeSnapshotTests(
    @param:TestParameter private val locale: Locale,
) {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = Television.copy(locale = locale.tag))

    @Test
    fun `night with moon`() {
        paparazzi.snapshot { HomeNightPreview() }
    }

    @Test
    fun `rain`() {
        paparazzi.snapshot { HomeRainPreview() }
    }

    @Test
    fun `storm`() {
        paparazzi.snapshot { HomeStormPreview() }
    }

    @Test
    fun `oud en nieuw`() {
        paparazzi.snapshot { HomeNewYearsEvePreview() }
    }

    @Test
    fun `koningsdag`() {
        paparazzi.snapshot { HomeKingsDayPreview() }
    }

    @Test
    fun `npo tv focused`() {
        paparazzi.snapshot { HomePreview(App.NpoTv) }
    }

    @Test
    fun `netflix focused`() {
        paparazzi.snapshot { HomePreview(App.Netflix) }
    }

    @Test
    fun `disney plus focused`() {
        paparazzi.snapshot { HomePreview(App.DisneyPlus) }
    }

    @Test
    fun `plex focused`() {
        paparazzi.snapshot { HomePreview(App.Plex) }
    }

    @Test
    fun `youtube focused`() {
        paparazzi.snapshot { HomePreview(App.YouTube) }
    }

    @Test
    fun `settings focused`() {
        paparazzi.snapshot { HomeSettingsFocusedPreview() }
    }
}
