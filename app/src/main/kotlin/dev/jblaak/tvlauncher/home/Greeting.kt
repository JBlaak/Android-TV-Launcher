package dev.jblaak.tvlauncher.home

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * The greeting above the clock: the time of day, or a special day's own words.
 */
internal sealed interface Greeting {
    data class Plain(@param:StringRes val text: Int) : Greeting

    /**
     * [text] is "Almost %d" or "Happy %d", filled with [year].
     */
    data class WithYear(@param:StringRes val text: Int, val year: Int) : Greeting
}

/**
 * What a special day adds to the sky.
 */
internal enum class Celebration {
    Fireworks,
    Bunting,
}

@Composable
internal fun Greeting.text(): String = when (this) {
    is Greeting.Plain -> stringResource(text)
    is Greeting.WithYear -> stringResource(text, year)
}
