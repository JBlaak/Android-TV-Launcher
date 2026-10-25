package dev.jblaak.tvlauncher.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jblaak.tvlauncher.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * A glass pill with the weather icon, the condition, the temperature when it's known and the next sunrise or
 * sunset: "Rain 9° · Sunset 17:18".
 */
@Composable
internal fun WeatherPill(
    weather: PillWeather,
    sunTime: SunTime?,
    modifier: Modifier,
) {
    val color = Color.White.copy(alpha = 0.85f)
    Row(
        modifier = modifier
            .frostedGlass(CircleShape, radius = 26.dp)
            .background(Color.White.copy(alpha = 0.08f), CircleShape)
            .border(0.75.dp, Color.White.copy(alpha = 0.16f), CircleShape)
            .padding(horizontal = 15.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(7.5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(weather.icon),
            contentDescription = null,
            modifier = Modifier.size(16.5.dp),
            colorFilter = ColorFilter.tint(color),
        )
        BasicText(
            text = pillText(weather, sunTime),
            style = TextStyle(color = color, fontSize = 13.5.sp, fontWeight = FontWeight.Medium),
        )
    }
}

@Composable
private fun pillText(weather: PillWeather, sunTime: SunTime?): String {
    val condition = stringResource(weather.condition)
    val conditionAndTemperature = weather.temperature?.let { stringResource(R.string.condition_and_temperature, condition, it) } ?: condition
    val sun = sunTime?.let { stringResource(it.label, it.time.format(DateTimeFormatter.ofPattern("HH:mm"))) }
    return sun?.let { stringResource(R.string.dot_separated, conditionAndTemperature, it) } ?: conditionAndTemperature
}

@Preview
@Composable
internal fun WeatherPillPreview() {
    Box(modifier = Modifier.background(Color(0xFF101224)).padding(20.dp)) {
        WeatherPill(
            weather = PillWeather(R.drawable.ic_weather_rain, R.string.rain, temperature = 9),
            sunTime = SunTime(R.string.sunset_time, LocalTime.of(17, 18)),
            modifier = Modifier,
        )
    }
}

@Preview
@Composable
internal fun WeatherPillWithoutTemperaturePreview() {
    Box(modifier = Modifier.background(Color(0xFF101224)).padding(20.dp)) {
        WeatherPill(
            weather = PillWeather(R.drawable.ic_weather_moon, R.string.clear, temperature = null),
            sunTime = SunTime(R.string.sunrise_time, LocalTime.of(7, 16)),
            modifier = Modifier,
        )
    }
}
