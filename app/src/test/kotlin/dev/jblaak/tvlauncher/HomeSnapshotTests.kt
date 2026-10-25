package dev.jblaak.tvlauncher

import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import kotlin.test.Test

class HomeSnapshotTests {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = Television)

    @Test
    fun `empty home`() {
        paparazzi.snapshot { HomePreview() }
    }
}
