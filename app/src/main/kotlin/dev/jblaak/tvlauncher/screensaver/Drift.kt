package dev.jblaak.tvlauncher.screensaver

import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.floor

internal data class DriftSpot(val x: Float, val y: Float)

internal fun driftSpot(at: LocalDateTime): DriftSpot = r2SequencePoint(at.toEpochSecond(ZoneOffset.UTC) / 60)

private fun r2SequencePoint(index: Long): DriftSpot = DriftSpot(x = fraction(index * 0.7548776662466927), y = fraction(index * 0.5698402909980532))

private fun fraction(value: Double): Float = (value - floor(value)).toFloat()
