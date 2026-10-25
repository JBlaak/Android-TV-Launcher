package dev.jblaak.tvlauncher.home

import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import dev.jblaak.tvlauncher.Locale
import dev.jblaak.tvlauncher.Television
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
internal class WeatherPillSnapshotTests(
    @param:TestParameter private val locale: Locale,
) {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = Television.copy(locale = locale.tag),
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun `rain with its temperature and the sunset`() {
        paparazzi.snapshot { WeatherPillPreview() }
    }

    @Test
    fun `a clear night without a temperature`() {
        paparazzi.snapshot { WeatherPillWithoutTemperaturePreview() }
    }
}
