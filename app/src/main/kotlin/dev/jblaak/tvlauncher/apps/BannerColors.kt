package dev.jblaak.tvlauncher.apps

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import dev.jblaak.tvlauncher.home.BrandColors

internal fun bannerColors(pixels: List<Color>): BrandColors {
    val opaque = pixels.filter { it.alpha > 0.5f }
    val glow = weightedAverage(opaque) { it.colourfulness() } ?: weightedAverage(opaque) { 1f } ?: Color.Gray
    return BrandColors(
        gradient = listOf(lerp(glow, Color.White, 0.25f), glow, lerp(glow, Color.Black, 0.6f)),
        glow = glow,
    )
}

private fun Color.colourfulness(): Float {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    return max - min
}

private fun weightedAverage(colors: List<Color>, weight: (Color) -> Float): Color? {
    val total = colors.sumOf { weight(it).toDouble() }
    if (total < 0.001) return null
    fun average(component: (Color) -> Float) = (colors.sumOf { weight(it) * component(it).toDouble() } / total).toFloat()
    return Color(average { it.red }, average { it.green }, average { it.blue })
}
