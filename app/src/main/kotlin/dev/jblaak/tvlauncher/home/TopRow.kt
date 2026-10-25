package dev.jblaak.tvlauncher.home

import android.text.format.DateFormat
import android.view.KeyEvent.KEYCODE_DPAD_DOWN
import android.view.KeyEvent.KEYCODE_DPAD_LEFT
import android.view.KeyEvent.KEYCODE_DPAD_RIGHT
import android.view.KeyEvent.KEYCODE_DPAD_UP
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jblaak.tvlauncher.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
internal fun TopRow(
    time: LocalTime,
    date: LocalDate,
    greeting: Greeting,
    weather: PillWeather?,
    sunTime: SunTime?,
    gearFocused: Boolean,
    onFocusSettings: () -> Unit,
    onLeaveSettings: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.25.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        GreetingAndClock(time = time, date = date, greeting = greeting)
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
            if (weather != null) WeatherPill(weather = weather, sunTime = sunTime, modifier = Modifier)
            SettingsGear(focused = gearFocused, onFocus = onFocusSettings, onLeave = onLeaveSettings, onOpen = onOpenSettings)
        }
    }
}

@Composable
private fun GreetingAndClock(time: LocalTime, date: LocalDate, greeting: Greeting) {
    val locale = LocalConfiguration.current.locales[0]
    val dateFormat = remember(locale) { DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "EEEdMMM"), locale) }
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        BasicText(
            text = stringResource(R.string.dot_separated, greeting.text(), date.format(dateFormat)),
            style = TextStyle(fontFamily = Figtree, color = Color.White.copy(alpha = 0.75f), fontSize = 16.5.sp, fontWeight = FontWeight.Medium),
        )
        BasicText(
            text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
            style = TextStyle(
                fontFamily = Figtree,
                color = Color.White,
                fontSize = 84.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-3.75).sp,
                lineHeight = 80.sp,
            ),
        )
    }
}

@Composable
private fun SettingsGear(
    focused: Boolean,
    onFocus: () -> Unit,
    onLeave: () -> Unit,
    onOpen: () -> Unit,
) {
    val focus by animateFloatAsState(if (focused) 1f else 0f, tween(250), label = "gear focus")
    val description = stringResource(R.string.settings)
    Box(
        modifier = Modifier
            .scale(1f + 0.1f * focus)
            .size(37.5.dp)
            .dropShadow(
                CircleShape,
                Shadow(radius = 11.25.dp, color = Color(0xFFB8C0D0).copy(alpha = 0.6f * focus), spread = (-6).dp, offset = DpOffset(0.dp, 9.dp)),
            )
            .gearRings(focus)
            .frostedGlass(CircleShape, radius = 26.dp)
            .background(lerp(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.94f), focus), CircleShape)
            .semantics { contentDescription = description }
            .onFocusChanged { if (it.isFocused) onFocus() }
            .onPreviewKeyEvent { event ->
                val code = event.key.nativeKeyCode
                if (event.type == KeyEventType.KeyDown && code == KEYCODE_DPAD_DOWN) onLeave()
                code in setOf(KEYCODE_DPAD_DOWN, KEYCODE_DPAD_UP, KEYCODE_DPAD_LEFT, KEYCODE_DPAD_RIGHT)
            }
            .opensOnPress(onOpen)
            .focusable(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_settings),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            colorFilter = ColorFilter.tint(lerp(Color.White.copy(alpha = 0.85f), Color(0xFF17131F), focus)),
        )
    }
}

private fun Modifier.gearRings(focus: Float): Modifier = drawBehind {
    val radius = size.minDimension / 2
    val edge = 0.75.dp.toPx()
    drawCircle(Color.White.copy(alpha = 0.16f * (1 - focus)), radius - edge / 2, style = Stroke(edge))
    val gap = 2.25.dp.toPx()
    val ring = 1.5.dp.toPx()
    drawCircle(Color.Black.copy(alpha = 0.25f * focus), radius + gap / 2, style = Stroke(gap))
    drawCircle(Color.White.copy(alpha = 0.85f * focus), radius + gap + ring / 2, style = Stroke(ring))
}

@Preview(widthDp = 960, heightDp = 160)
@Composable
internal fun TopRowPreview() {
    Box(modifier = Modifier.background(Color(0xFF101224)).padding(20.dp)) {
        TopRow(
            time = HomeState.previewValue.time,
            date = HomeState.previewValue.date,
            greeting = HomeState.previewValue.greeting,
            weather = HomeState.previewValue.weather,
            sunTime = HomeState.previewValue.sunTime,
            gearFocused = true,
            onFocusSettings = {},
            onLeaveSettings = {},
            onOpenSettings = {},
        )
    }
}

@Preview
@Composable
internal fun GreetingAndClockPreview() {
    Box(modifier = Modifier.background(Color(0xFF101224)).padding(20.dp)) {
        GreetingAndClock(time = HomeState.previewValue.time, date = HomeState.previewValue.date, greeting = HomeState.previewValue.greeting)
    }
}

@Preview
@Composable
internal fun SettingsGearPreview() {
    Row(modifier = Modifier.background(Color(0xFF101224)).padding(20.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        SettingsGear(focused = false, onFocus = {}, onLeave = {}, onOpen = {})
        SettingsGear(focused = true, onFocus = {}, onLeave = {}, onOpen = {})
    }
}
