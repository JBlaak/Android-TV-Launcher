package dev.jblaak.tvlauncher.home

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.jblaak.tvlauncher.R
import dev.jblaak.tvlauncher.sky.Moment
import dev.jblaak.tvlauncher.sky.moment
import java.time.ZonedDateTime

internal sealed interface Greeting {
    data class Plain(@param:StringRes val text: Int) : Greeting

    data class WithYear(@param:StringRes val textWithYear: Int, val year: Int) : Greeting
}

internal enum class Celebration {
    Fireworks,
    Bunting,
}

@Composable
internal fun Greeting.text(): String = when (this) {
    is Greeting.Plain -> stringResource(text)
    is Greeting.WithYear -> stringResource(textWithYear, year)
}

internal fun greeting(at: ZonedDateTime): Greeting = when (val moment = moment(at)) {
    is Moment.NewYearsEve -> Greeting.WithYear(R.string.almost_year, moment.comingYear)
    is Moment.NewYearsDay -> Greeting.WithYear(R.string.happy_year, moment.year)
    Moment.KingsDay -> Greeting.Plain(R.string.fijne_koningsdag)
    Moment.ClocksWentBack -> Greeting.Plain(R.string.clocks_went_back)
    Moment.ClocksWentForward -> Greeting.Plain(R.string.clocks_went_forward)
    null -> Greeting.Plain(timeOfDay(at))
}

private fun timeOfDay(at: ZonedDateTime): Int = when (at.hour) {
    in 0..5 -> R.string.good_night
    in 6..11 -> R.string.good_morning
    in 12..17 -> R.string.good_afternoon
    else -> R.string.good_evening
}

internal fun celebration(at: ZonedDateTime): Celebration? = when (moment(at)) {
    is Moment.NewYearsEve, is Moment.NewYearsDay -> Celebration.Fireworks
    Moment.KingsDay -> Celebration.Bunting
    Moment.ClocksWentBack, Moment.ClocksWentForward, null -> null
}
