package dev.jblaak.tvlauncher.sky

/**
 * What the window shows at one moment. Positions are in pixels on a 1280×720 reference screen, the size of the
 * design mock-ups; the renderer scales them to the real screen.
 */
data class SkyScene(
    val colors: SkyColors,
    val sun: Sun,
    val moon: Moon,
)

/**
 * The sky's [base] colour and two soft glows: [warm] around [warmCentre], which follows the sun, and [cool] around
 * [coolCentre] on the opposite side. [strength] scales both glows.
 */
data class SkyColors(
    val base: Rgb,
    val warm: Rgb,
    val warmCentre: ScreenPoint,
    val cool: Rgb,
    val coolCentre: ScreenPoint,
    val strength: Double,
)

data class Rgb(
    val red: Int,
    val green: Int,
    val blue: Int,
)

data class ScreenPoint(
    val x: Double,
    val y: Double,
)

sealed interface Sun {

    /**
     * The sun is in view at [position]. [intensity] scales its glow, rays, halo and flare; [coreOpacity] is the
     * opacity of its hot core.
     */
    data class Disc(
        val position: ScreenPoint,
        val intensity: Double,
        val coreOpacity: Double,
        val colors: SunColors,
    ) : Sun

    /**
     * The sun is up but behind the viewer: no disc, only its light falling in from the top.
     */
    data object BehindViewer : Sun

    data object BelowHorizon : Sun
}

data class SunColors(
    val glow: Rgb,
    val core: Rgb,
)

sealed interface Moon {

    data class Disc(
        val position: ScreenPoint,
        val opacity: Double,
        val phase: MoonPhase,
    ) : Moon

    data class Glow(
        val position: ScreenPoint,
        val opacity: Double,
    ) : Moon

    data object Hidden : Moon
}

data class MoonPhase(
    val ageInDays: Double,
    val litFraction: Double,
    val waxing: Boolean,
)
