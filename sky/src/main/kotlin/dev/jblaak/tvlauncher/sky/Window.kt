package dev.jblaak.tvlauncher.sky

/**
 * A place on Earth, in degrees: north and east are positive.
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
)

/**
 * The screen seen as a window at [location] that looks toward the compass direction [facing], in degrees clockwise
 * from north.
 */
data class Window(
    val location: Location,
    val facing: Double,
)
