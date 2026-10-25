package dev.jblaak.tvlauncher.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun Home(
    state: HomeState,
    onFocus: (App) -> Unit,
    onOpen: (App) -> Unit,
    onFocusSettings: () -> Unit,
    onLeaveSettings: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    SkyBackground(
        scene = state.sky,
        celebration = state.celebration,
        appGlow = if (state.gearFocused) Color(0xFFB8C0D0) else state.tiles.firstOrNull { it.app == state.focused }?.colors?.glow ?: Color.Transparent,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 54.dp, vertical = 48.dp),
        ) {
            TopRow(
                time = state.time,
                date = state.date,
                greeting = state.greeting,
                sunTime = state.sunTime,
                gearFocused = state.gearFocused,
                onFocusSettings = onFocusSettings,
                onLeaveSettings = onLeaveSettings,
                onOpenSettings = onOpenSettings,
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 54.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.5.dp),
        ) {
            FocusedAppLabel(label = state.label)
            Dock(tiles = state.tiles, focused = state.focusedTile, onFocus = onFocus, onOpen = onOpen)
        }
    }
}

@Composable
private fun FocusedAppLabel(label: FocusLabel?) {
    AnimatedContent(
        targetState = label,
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
        label = "focused app",
    ) { current ->
        Row(
            modifier = Modifier.padding(horizontal = 14.25.dp),
            horizontalArrangement = Arrangement.spacedBy(10.5.dp),
        ) {
            if (current != null) {
                BasicText(
                    text = stringResource(current.name),
                    modifier = Modifier.alignByBaseline(),
                    style = TextStyle(color = Color.White, fontSize = 25.5.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.75).sp),
                )
                BasicText(
                    text = stringResource(current.description),
                    modifier = Modifier.alignByBaseline(),
                    style = TextStyle(color = Color.White.copy(alpha = 0.75f), fontSize = 15.sp, fontWeight = FontWeight.Medium),
                )
            }
        }
    }
}

@Preview(device = "id:tv_1080p")
@Composable
internal fun HomePreview(
    @PreviewParameter(FocusedAppProvider::class) focused: App,
) {
    Home(
        state = HomeState.previewValue.copy(focused = focused),
        onFocus = {},
        onOpen = {},
        onFocusSettings = {},
        onLeaveSettings = {},
        onOpenSettings = {},
    )
}

@Preview(device = "id:tv_1080p")
@Composable
internal fun HomeSettingsFocusedPreview() {
    Home(
        state = HomeState.previewValue.copy(focused = App.Netflix, gearFocused = true),
        onFocus = {},
        onOpen = {},
        onFocusSettings = {},
        onLeaveSettings = {},
        onOpenSettings = {},
    )
}

@Preview(device = "id:tv_1080p")
@Composable
internal fun HomeNightPreview() {
    Home(
        state = HomeState.nightPreviewValue,
        onFocus = {},
        onOpen = {},
        onFocusSettings = {},
        onLeaveSettings = {},
        onOpenSettings = {},
    )
}

@Preview(device = "id:tv_1080p")
@Composable
internal fun HomeNewYearsEvePreview() {
    Home(
        state = HomeState.newYearsEvePreviewValue,
        onFocus = {},
        onOpen = {},
        onFocusSettings = {},
        onLeaveSettings = {},
        onOpenSettings = {},
    )
}

@Preview(device = "id:tv_1080p")
@Composable
internal fun HomeKingsDayPreview() {
    Home(
        state = HomeState.kingsDayPreviewValue,
        onFocus = {},
        onOpen = {},
        onFocusSettings = {},
        onLeaveSettings = {},
        onOpenSettings = {},
    )
}

internal class FocusedAppProvider : PreviewParameterProvider<App> {
    override val values = App.entries.asSequence()
}
