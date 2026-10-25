package dev.jblaak.tvlauncher.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jblaak.tvlauncher.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
internal fun WeatherPill(
    sunTime: SunTime,
    modifier: Modifier,
) {
    BasicText(
        text = stringResource(sunTime.label, sunTime.time.format(DateTimeFormatter.ofPattern("HH:mm"))),
        modifier = modifier
            .background(Color.White.copy(alpha = 0.08f), CircleShape)
            .border(0.75.dp, Color.White.copy(alpha = 0.16f), CircleShape)
            .padding(horizontal = 15.dp, vertical = 9.dp),
        style = TextStyle(color = Color.White.copy(alpha = 0.85f), fontSize = 13.5.sp, fontWeight = FontWeight.Medium),
    )
}

@Preview
@Composable
internal fun WeatherPillPreview() {
    Box(modifier = Modifier.background(Color(0xFF101224)).padding(20.dp)) {
        WeatherPill(sunTime = SunTime(R.string.sunset_time, LocalTime.of(17, 18)), modifier = Modifier)
    }
}
