package dev.jblaak.tvlauncher.sky.internal

import kotlin.math.floor
import kotlin.math.sin

/**
 * A fixed pseudo-random number in 0..1 for seed [i], the mock-ups' `rnd(i)` (the classic GLSL sine hash), so every
 * particle lands in the same place every time.
 */
internal fun seeded(i: Int): Double {
    val x = sin(i * 12.9898 + 78.233) * 43758.5453
    return x - floor(x)
}
