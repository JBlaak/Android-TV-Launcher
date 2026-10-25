package dev.jblaak.tvlauncher.sky

import dev.jblaak.tvlauncher.sky.internal.seeded
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * One firework over the neighbourhood, at [centre]. Every [periodSeconds] it blooms and throws its [sparks], each to
 * an offset from the centre. Its animation runs [phaseSeconds] ahead, so the bursts go off out of step.
 */
data class Burst(
    val centre: ScreenPoint,
    val color: Rgb,
    val periodSeconds: Double,
    val phaseSeconds: Double,
    val sparks: List<ScreenPoint>,
)

/**
 * One triangular flag of the Koningsdag bunting, hanging from [anchor], the middle of its top edge. It hangs at
 * [rotation] degrees clockwise, along the rope, and sways ±5° from there, one way in [swaySeconds] and back.
 */
data class Flag(
    val anchor: ScreenPoint,
    val rotation: Double,
    val color: Rgb,
    val swaySeconds: Double,
    val phaseSeconds: Double,
)

/**
 * Five bursts with fixed seeds (design canvas `whimsyFx`, Oud & Nieuw).
 */
fun fireworks(): List<Burst> {
    val colors = listOf(Rgb(255, 210, 120), Rgb(255, 120, 160), Rgb(140, 220, 255), Rgb(190, 255, 160), Rgb(255, 255, 255))
    return colors.mapIndexed { burst, color ->
        val period = 2.8 + seeded(burst + 802) * 1.4
        Burst(
            centre = ScreenPoint(180 + seeded(burst + 800) * 920, 90 + seeded(burst + 801) * 220),
            color = color,
            periodSeconds = period,
            phaseSeconds = seeded(burst + 803) * period,
            sparks = List(20) { spark ->
                val angle = spark / 20.0 * 2 * PI
                val distance = 90 + seeded(burst * 20 + spark + 810) * 40
                ScreenPoint(cos(angle) * distance, sin(angle) * distance)
            },
        )
    }
}

/**
 * 24 flags, 56 px apart on a rope that sags 34 px in a half sine across the top (design canvas `whimsyFx`,
 * Koningsdag).
 */
fun bunting(): List<Flag> {
    val colors = listOf(Rgb(0xE8, 0x11, 0x2D), Rgb(0xFF, 0xFF, 0xFF), Rgb(0x21, 0x46, 0x8B), Rgb(0xFF, 0x7F, 0x00))
    return List(24) { flag ->
        val left = -10.0 + flag * 56
        val along = PI * (left + 10) / 1300
        Flag(
            anchor = ScreenPoint(left + 15, 6 + 34 * sin(along)),
            rotation = cos(along) * 34 * PI / 1300 * 180 / PI,
            color = colors[flag % colors.size],
            swaySeconds = 1.6 + seeded(flag + 900),
            phaseSeconds = seeded(flag + 901) * 2,
        )
    }
}
