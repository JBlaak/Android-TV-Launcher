package dev.jblaak.tvlauncher.screensaver

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jblaak.tvlauncher.home.HomeState
import dev.jblaak.tvlauncher.home.PillWeather
import dev.jblaak.tvlauncher.home.SkyBackground
import dev.jblaak.tvlauncher.home.SunTime
import dev.jblaak.tvlauncher.home.pillText
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
internal fun Screensaver(state: HomeState) {
    val spot = driftSpot(LocalDateTime.of(state.date, state.time))
    val x by animateFloatAsState(spot.x, tween(20_000, easing = FastOutSlowInEasing), label = "drift x")
    val y by animateFloatAsState(spot.y, tween(20_000, easing = FastOutSlowInEasing), label = "drift y")
    SkyBackground(
        scene = state.sky,
        celebration = state.celebration,
        appGlow = Color.Transparent,
        modifier = Modifier.fillMaxSize(),
    ) {
        ClockAndWeather(
            time = state.time,
            weather = state.weather,
            sunTime = state.sunTime,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 54.dp, vertical = 48.dp)
                .drift(x, y),
        )
    }
}

@Composable
private fun ClockAndWeather(
    time: LocalTime,
    weather: PillWeather?,
    sunTime: SunTime?,
    modifier: Modifier,
) {
    val shadow = Shadow(Color.Black.copy(alpha = 0.35f), blurRadius = 16f)
    val color = Color.White.copy(alpha = 0.85f)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        BasicText(
            text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
            style = TextStyle(color = Color.White, fontSize = 54.sp, fontWeight = FontWeight.Light, letterSpacing = (-2.25).sp, shadow = shadow),
        )
        if (weather != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.5.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(weather.icon),
                    contentDescription = null,
                    modifier = Modifier.size(16.5.dp),
                    colorFilter = ColorFilter.tint(color),
                )
                BasicText(
                    text = pillText(weather, sunTime),
                    style = TextStyle(color = color, fontSize = 15.sp, fontWeight = FontWeight.Medium, shadow = shadow),
                )
            }
        }
    }
}

private fun Modifier.drift(x: Float, y: Float): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    layout(constraints.maxWidth, constraints.maxHeight) {
        placeable.place(
            x = ((constraints.maxWidth - placeable.width) * x).roundToInt(),
            y = ((constraints.maxHeight - placeable.height) * y).roundToInt(),
        )
    }
}

@Preview(device = "id:tv_1080p")
@Composable
internal fun ScreensaverPreview() {
    Screensaver(state = HomeState.rainPreviewValue)
}

@Preview
@Composable
internal fun ClockAndWeatherPreview() {
    Box(modifier = Modifier.background(Color(0xFF101224)).padding(20.dp)) {
        ClockAndWeather(
            time = HomeState.rainPreviewValue.time,
            weather = HomeState.rainPreviewValue.weather,
            sunTime = HomeState.rainPreviewValue.sunTime,
            modifier = Modifier,
        )
    }
}
