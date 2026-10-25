package dev.jblaak.tvlauncher.sky

/**
 * The wind [from] a compass direction in degrees clockwise from north, as weather data gives it, at [speed] with
 * [gusts] up to, both in km/h.
 */
data class Wind(
    val from: Double,
    val speed: Double,
    val gusts: Double,
)
