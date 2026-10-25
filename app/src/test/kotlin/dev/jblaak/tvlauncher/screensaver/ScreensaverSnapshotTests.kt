package dev.jblaak.tvlauncher.screensaver

import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import dev.jblaak.tvlauncher.Locale
import dev.jblaak.tvlauncher.Television
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
internal class ScreensaverSnapshotTests(
    @param:TestParameter private val locale: Locale,
) {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = Television.copy(locale = locale.tag))

    @Test
    fun `screensaver`() {
        paparazzi.snapshot { ScreensaverPreview() }
    }
}
