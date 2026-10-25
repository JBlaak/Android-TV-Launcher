package dev.jblaak.tvlauncher.home

import app.cash.paparazzi.Paparazzi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import dev.jblaak.tvlauncher.Television
import dev.jblaak.tvlauncher.sky.SkyWeather
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
internal class WeatherEffectsSnapshotTests(
    @param:TestParameter private val weather: SkyWeather,
) {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = Television)

    @Test
    fun `the weather at night`() {
        paparazzi.snapshot { WeatherEffectsPreview(weather) }
    }
}
