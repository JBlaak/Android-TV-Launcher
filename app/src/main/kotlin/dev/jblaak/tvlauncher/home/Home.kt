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
) {
    val focusedTile = state.tiles.firstOrNull { it.app == state.focused }
    Box(modifier = Modifier.fillMaxSize()) {
        SkyBackground(
            scene = state.sky,
            appGlow = focusedTile?.colors?.glow ?: Color.Transparent,
            modifier = Modifier.fillMaxSize(),
        )
        state.sunTime?.let { sunTime ->
            WeatherPill(
                sunTime = sunTime,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 68.25.dp, vertical = 48.dp),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 54.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.5.dp),
        ) {
            FocusedAppLabel(tile = focusedTile)
            Dock(tiles = state.tiles, focused = state.focused, onFocus = onFocus, onOpen = onOpen)
        }
    }
}

@Composable
private fun FocusedAppLabel(tile: Tile?) {
    AnimatedContent(
        targetState = tile,
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
        contentKey = { it?.app },
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
    Home(state = HomeState.previewValue.copy(focused = focused), onFocus = {}, onOpen = {})
}

@Preview(device = "id:tv_1080p")
@Composable
internal fun HomeNightPreview() {
    Home(state = HomeState.nightPreviewValue, onFocus = {}, onOpen = {})
}

internal class FocusedAppProvider : PreviewParameterProvider<App> {
    override val values = App.entries.asSequence()
}
