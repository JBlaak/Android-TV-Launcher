package dev.jblaak.tvlauncher.home

import app.cash.paparazzi.Paparazzi
import dev.jblaak.tvlauncher.Television
import org.junit.Rule
import org.junit.Test

internal class SkyLifeSnapshotTests {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = Television)

    @Test
    fun `a shooting star and geese`() {
        paparazzi.snapshot { SkyLifePreview() }
    }
}
