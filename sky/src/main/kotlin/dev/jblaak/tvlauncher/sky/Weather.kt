package dev.jblaak.tvlauncher.sky

/**
 * The seven kinds of weather the sky can show. [Warm] is clear weather that's hot enough to shimmer.
 */
enum class Weather {
    Clear,
    Warm,
    Cloudy,
    Rain,
    Storm,
    Snow,
    Fog,
}

/**
 * The wind [from] a compass direction in degrees clockwise from north, as weather data gives it, at [speed] with
 * [gusts] up to, both in km/h.
 */
data class Wind(
    val from: Double,
    val speed: Double,
    val gusts: Double,
)
