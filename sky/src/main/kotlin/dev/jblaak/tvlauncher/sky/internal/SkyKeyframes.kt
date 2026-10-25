package dev.jblaak.tvlauncher.sky.internal

import dev.jblaak.tvlauncher.sky.Rgb
import kotlin.math.roundToInt

internal data class SkyKeyframe(
    val base: Rgb,
    val warm: Rgb,
    val cool: Rgb,
    val strength: Double,
)

/**
 * Sky colours by sun elevation from docs/DESIGN.md, plus the deepest-night keyframe at −90° from the v1 mock-up.
 * Each channel blends linearly between neighbouring keyframes and is rounded.
 */
private val keyframes = listOf(
    -90.0 to SkyKeyframe(Rgb(13, 14, 26), Rgb(70, 60, 150), Rgb(30, 90, 120), 0.35),
    -12.0 to SkyKeyframe(Rgb(16, 18, 36), Rgb(80, 70, 170), Rgb(40, 100, 140), 0.40),
    -6.0 to SkyKeyframe(Rgb(26, 30, 64), Rgb(90, 100, 200), Rgb(160, 100, 210), 0.55),
    -1.0 to SkyKeyframe(Rgb(50, 30, 58), Rgb(255, 125, 85), Rgb(205, 85, 150), 0.60),
    5.0 to SkyKeyframe(Rgb(56, 42, 50), Rgb(255, 175, 95), Rgb(230, 125, 115), 0.55),
    15.0 to SkyKeyframe(Rgb(32, 48, 72), Rgb(115, 175, 235), Rgb(125, 210, 200), 0.50),
    90.0 to SkyKeyframe(Rgb(40, 60, 88), Rgb(145, 200, 245), Rgb(155, 225, 215), 0.50),
)

internal fun skyKeyframe(elevation: Double): SkyKeyframe {
    val upper = keyframes.indexOfFirst { (keyframeElevation, _) -> elevation <= keyframeElevation }
    if (upper == -1) return keyframes.last().second
    if (upper == 0) return keyframes.first().second
    val (fromElevation, from) = keyframes[upper - 1]
    val (toElevation, to) = keyframes[upper]
    val fraction = (elevation - fromElevation) / (toElevation - fromElevation)
    return SkyKeyframe(
        base = from.base.blend(to.base, fraction),
        warm = from.warm.blend(to.warm, fraction),
        cool = from.cool.blend(to.cool, fraction),
        strength = from.strength + (to.strength - from.strength) * fraction,
    )
}

private fun Rgb.blend(other: Rgb, fraction: Double): Rgb = Rgb(
    red = blend(red, other.red, fraction),
    green = blend(green, other.green, fraction),
    blue = blend(blue, other.blue, fraction),
)

private fun blend(from: Int, to: Int, fraction: Double): Int = (from + (to - from) * fraction).roundToInt()
