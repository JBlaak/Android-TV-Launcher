package dev.jblaak.tvlauncher.sky

/**
 * The weather playing across the window, back to front (v1 mock-up: weatherFx). Every particle carries its own
 * loop: it is [periodSeconds] long and runs `phaseSeconds` ahead, so the particles move out of step. Positions and
 * sizes are in reference pixels; angles in degrees, positive leaning or moving to the right.
 */
data class WeatherEffects(
    val stars: List<Star>,
    val heatHaze: Boolean,
    val scud: List<Scud>,
    val curtains: List<Curtain>,
    val leaves: List<Leaf>,
    val clouds: List<Cloud>,
    val rain: List<Drop>,
    val lightning: Boolean,
    val snow: List<Flake>,
) {
    companion object {
        val None = WeatherEffects(
            stars = emptyList(),
            heatHaze = false,
            scud = emptyList(),
            curtains = emptyList(),
            leaves = emptyList(),
            clouds = emptyList(),
            rain = emptyList(),
            lightning = false,
            snow = emptyList(),
        )
    }
}

/**
 * A star of [size] px at [centre] that twinkles between 15% and 90% opacity, one way in [periodSeconds] and back.
 */
data class Star(
    val centre: ScreenPoint,
    val size: Double,
    val periodSeconds: Double,
    val phaseSeconds: Double,
)

/**
 * A soft cloud or fog band: an ellipse in the box at [topLeft], blurred by [blur] px, that sways 90 px either side,
 * one way in [periodSeconds] and back.
 */
data class Cloud(
    val topLeft: ScreenPoint,
    val width: Double,
    val height: Double,
    val blur: Double,
    val color: Rgb,
    val opacity: Double,
    val periodSeconds: Double,
    val phaseSeconds: Double,
)

/**
 * A low, dark storm cloud racing from [fromX] to [toX] (its left edge) at height [top] with the wind, blurred by 40 px.
 */
data class Scud(
    val top: Double,
    val width: Double,
    val height: Double,
    val opacity: Double,
    val fromX: Double,
    val toX: Double,
    val periodSeconds: Double,
    val phaseSeconds: Double,
)

/**
 * A 1100×900 curtain of rain sweeping from [fromX] to [toX], slanted by [skew] like the drops.
 */
data class Curtain(
    val fromX: Double,
    val toX: Double,
    val skew: Double,
    val periodSeconds: Double,
    val phaseSeconds: Double,
)

/**
 * A leaf tumbling from [start] across the screen in [direction] (+1 to the right, −1 to the left).
 */
data class Leaf(
    val start: ScreenPoint,
    val width: Double,
    val height: Double,
    val color: Rgb,
    val direction: Int,
    val periodSeconds: Double,
    val phaseSeconds: Double,
)

/**
 * A raindrop streak [length] px long that falls from 140 px above the screen to 880 px below its top, starting at
 * [startX] and moving [drift] px sideways on the way, leaning [tilt] degrees.
 */
data class Drop(
    val startX: Double,
    val length: Double,
    val opacity: Double,
    val tilt: Double,
    val drift: Double,
    val periodSeconds: Double,
    val phaseSeconds: Double,
)

/**
 * A snowflake of [size] px that sways down from just above the screen at [startX], drifting [drift] px sideways by
 * the bottom. The biggest are [blurred].
 */
data class Flake(
    val startX: Double,
    val size: Double,
    val opacity: Double,
    val blurred: Boolean,
    val drift: Double,
    val periodSeconds: Double,
    val phaseSeconds: Double,
)

/**
 * The wind to assume for [weather] without live data; south-west is the prevailing direction here (v1 mock-up: WIND).
 */
fun typicalWind(weather: SkyWeather): Wind = when (weather) {
    SkyWeather.Storm -> Wind(from = 225.0, speed = 75.0, gusts = 75.0)
    SkyWeather.Rain -> Wind(from = 225.0, speed = 20.0, gusts = 20.0)
    SkyWeather.Snow -> Wind(from = 90.0, speed = 15.0, gusts = 15.0)
    SkyWeather.Clear, SkyWeather.Warm, SkyWeather.Clouds, SkyWeather.Fog -> Wind(from = 225.0, speed = 12.0, gusts = 12.0)
}
