package dev.jblaak.tvlauncher.apps

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Test

class BannerColorsTest {

    @Test
    fun `a banner glows in its most colourful colour`() {
        /* Given */
        val pixels = List(90) { Color.White } + List(10) { Color(0xFF1F6BFF) }

        /* When */
        val colors = bannerColors(pixels)

        /* Then */
        assertEquals(Color(0xFF1F6BFF), colors.glow)
    }

    @Test
    fun `a banner's gradient runs from light to dark through its colour`() {
        /* Given */
        val pixels = List(10) { Color(0xFF1F6BFF) }

        /* When */
        val colors = bannerColors(pixels)

        /* Then */
        assertEquals(Color(0xFF1F6BFF), colors.gradient[1])
        assertEquals(true, colors.gradient.first().luminance() > colors.gradient.last().luminance())
    }

    @Test
    fun `a grey banner glows in its average colour`() {
        /* Given */
        val pixels = List(5) { Color(0xFF808080) } + List(5) { Color(0xFF404040) }

        /* When */
        val colors = bannerColors(pixels)

        /* Then */
        assertEquals(Color(0xFF606060), colors.glow)
    }

    @Test
    fun `transparent pixels don't count`() {
        /* Given */
        val pixels = List(90) { Color.Transparent } + List(10) { Color(0xFF808080) }

        /* When */
        val colors = bannerColors(pixels)

        /* Then */
        assertEquals(Color(0xFF808080), colors.glow)
    }
}
