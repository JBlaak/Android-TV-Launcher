package dev.jblaak.tvlauncher.home

import app.cash.paparazzi.Paparazzi
import dev.jblaak.tvlauncher.Television
import dev.jblaak.tvlauncher.sky.SkyWeather
import org.junit.Rule
import org.junit.Test

internal class WindowGlassSnapshotTests {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = Television)

    @Test
    fun `rain beads on the glass`() {
        paparazzi.snapshot { WindowGlassPreview(SkyWeather.Rain) }
    }

    @Test
    fun `frost in the corners of the glass`() {
        paparazzi.snapshot { WindowGlassPreview(SkyWeather.Snow) }
    }
}
