package dev.jblaak.tvlauncher.home

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Easing
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The small move a logo makes when its tile gains focus, as a pose for each millisecond since then.
 * [Sparkle] keeps the logo still: Disney+ gets a light sweeping through its name instead, for as long as it has focus.
 */
internal enum class LogoMove(val durationMillis: Int) {
    Wobble(durationMillis = 1800) {
        private val rotation = Keyframes(900, iterations = 2, EaseInOut, 0f to 0f, 0.25f to -9f, 0.75f to 9f, 1f to 0f)

        override fun poseAt(millis: Int) = LogoPose.Rest.copy(rotation = rotation.valueAt(millis))
    },
    TaDum(durationMillis = 1100) {
        private val scale = Keyframes(550, iterations = 2, EaseOut, 0f to 1f, 0.3f to 1.2f, 0.55f to 0.96f, 1f to 1f)

        override fun poseAt(millis: Int) = LogoPose.Rest.copy(scale = scale.valueAt(millis))
    },
    Sparkle(durationMillis = 0) {
        override fun poseAt(millis: Int) = LogoPose.Rest
    },
    SlideIn(durationMillis = 500) {
        private val easing = CubicBezierEasing(0.2f, 1.3f, 0.4f, 1f)
        private val offset = Keyframes(500, iterations = 1, easing, 0f to -10.5f, 1f to 0f)
        private val alpha = Keyframes(500, iterations = 1, easing, 0f to 0.3f, 1f to 1f)

        override fun poseAt(millis: Int) = LogoPose.Rest.copy(
            offsetX = offset.valueAt(millis).dp,
            alpha = alpha.valueAt(millis).coerceAtMost(1f),
        )
    },
    Nudge(durationMillis = 1500) {
        private val offset = Keyframes(500, iterations = 3, EaseInOut, 0f to 0f, 0.4f to 5.25f, 1f to 0f)

        override fun poseAt(millis: Int) = LogoPose.Rest.copy(offsetX = offset.valueAt(millis).dp)
    },
    ;

    abstract fun poseAt(millis: Int): LogoPose
}

internal data class LogoPose(
    val rotation: Float,
    val scale: Float,
    val offsetX: Dp,
    val alpha: Float,
) {
    companion object {
        val Rest = LogoPose(rotation = 0f, scale = 1f, offsetX = 0.dp, alpha = 1f)
    }
}

internal fun App.logoMove(): LogoMove = when (this) {
    App.NpoTv -> LogoMove.Wobble
    App.Netflix -> LogoMove.TaDum
    App.DisneyPlus -> LogoMove.Sparkle
    App.Plex -> LogoMove.SlideIn
    App.YouTube -> LogoMove.Nudge
}

internal const val TileBounceMillis = 450

private val tileBounce = Keyframes(TileBounceMillis, iterations = 1, CubicBezierEasing(0.3f, 1.6f, 0.5f, 1f), 0f to 0.94f, 0.6f to 1.05f, 1f to 1f)

/**
 * The scale of a tile that just gained focus: it dips, overshoots and settles.
 */
internal fun tileBounceAt(millis: Int): Float = tileBounce.valueAt(millis)

/**
 * Values at fractions of one cycle, eased per segment between them as CSS keyframes are.
 */
private class Keyframes(
    private val cycleMillis: Int,
    private val iterations: Int,
    private val easing: Easing,
    private vararg val frames: Pair<Float, Float>,
) {
    fun valueAt(millis: Int): Float {
        if (millis >= cycleMillis * iterations) return frames.last().second
        val fraction = (millis.coerceAtLeast(0) % cycleMillis) / cycleMillis.toFloat()
        val end = frames.indexOfFirst { it.first > fraction }
        val (startAt, from) = frames[end - 1]
        val (endAt, to) = frames[end]
        return from + (to - from) * easing.transform((fraction - startAt) / (endAt - startAt))
    }
}
