package dev.jblaak.tvlauncher.home

import android.view.KeyEvent.KEYCODE_DPAD_CENTER
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.KeyEvent.KEYCODE_NUMPAD_ENTER
import android.view.ViewConfiguration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun Dock(
    tiles: List<Tile>,
    focused: App?,
    onFocus: (App) -> Unit,
    onOpen: (App) -> Unit,
) {
    val focusRequesters = remember { App.entries.associateWith { FocusRequester() } }
    LaunchedEffect(focused, tiles) {
        focused?.let { focusRequesters.getValue(it).requestFocus() }
    }
    val shape = RoundedCornerShape(30.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .dropShadow(shape, Shadow(radius = 22.5.dp, color = Color.Black.copy(alpha = 0.6f), spread = (-18).dp, offset = DpOffset(0.dp, 22.5.dp)))
            .background(Color.White.copy(alpha = 0.07f), shape)
            .border(1.dp, Color.White.copy(alpha = 0.16f), shape)
            .padding(13.5.dp),
        horizontalArrangement = Arrangement.spacedBy(13.5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tiles.forEach { tile ->
            DockTile(
                tile = tile,
                focused = tile.app == focused,
                focusRequester = focusRequesters.getValue(tile.app),
                onFocus = { onFocus(tile.app) },
                onOpen = { onOpen(tile.app) },
            )
        }
    }
}

@Composable
private fun RowScope.DockTile(
    tile: Tile,
    focused: Boolean,
    focusRequester: FocusRequester,
    onFocus: () -> Unit,
    onOpen: () -> Unit,
) {
    val focus by animateFloatAsState(if (focused) 1f else 0f, tween(300), label = "tile focus")
    val shape = RoundedCornerShape(19.5.dp)
    Box(
        modifier = Modifier
            .weight(1f + 0.7f * focus)
            .height(93.dp)
            .dropShadow(
                shape,
                Shadow(radius = 18.dp, color = tile.colors.glow.copy(alpha = 0.67f * focus), spread = (-9).dp, offset = DpOffset(0.dp, 15.dp)),
            )
            .tileBackground(tile.colors.gradient, focus)
            .focusRequester(focusRequester)
            .onFocusChanged { if (it.isFocused) onFocus() }
            .opensOnPress(onOpen)
            .focusable(),
        contentAlignment = Alignment.Center,
    ) {
        TileLogo(logo = tile.logo, focus = focus)
    }
}

@Composable
private fun TileLogo(logo: TileLogo, focus: Float) {
    when (logo) {
        is TileLogo.Mark -> Image(
            painter = painterResource(logo.drawable),
            contentDescription = null,
            modifier = Modifier.size(logo.size),
            colorFilter = logo.colors?.let { ColorFilter.tint(lerp(it.idle, it.focused, focus)) },
        )

        is TileLogo.Banner -> Image(
            painter = logo.painter,
            contentDescription = null,
            modifier = Modifier
                .height(54.dp)
                .width(96.dp)
                .clip(RoundedCornerShape(9.dp)),
            contentScale = ContentScale.Crop,
        )

        is TileLogo.Wordmark -> BasicText(
            text = stringResource(logo.name),
            style = TextStyle(
                color = logo.colors?.let { lerp(it.idle, it.focused, focus) } ?: Color.White,
                fontSize = logo.size.value.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.4).sp,
            ),
        )
    }
}

/**
 * Idle tiles are dark glass with a faint inner edge. A focused tile fills with the brand gradient at 150°, as CSS
 * draws it, inside a 2 dp white ring.
 */
private fun Modifier.tileBackground(gradient: List<Color>, focus: Float): Modifier = drawWithCache {
    val radius = 19.5.dp.toPx()
    val angle = 150 * PI / 180
    val direction = Offset(sin(angle).toFloat(), -cos(angle).toFloat())
    val halfLength = (abs(size.width * direction.x) + abs(size.height * direction.y)) / 2
    val center = size.center
    val brush = Brush.linearGradient(gradient, start = center - direction * halfLength, end = center + direction * halfLength)
    val tile = Path().apply { addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(radius))) }
    val innerEdge = 0.5.dp.toPx()
    val ring = 1.dp.toPx()
    onDrawBehind {
        drawPath(tile, Color.White.copy(alpha = 0.08f * (1 - focus)))
        drawPath(tile, brush, alpha = focus)
        drawRoundRect(
            color = Color.White.copy(alpha = 0.1f * (1 - focus)),
            topLeft = Offset(innerEdge, innerEdge),
            size = size.copy(width = size.width - 2 * innerEdge, height = size.height - 2 * innerEdge),
            cornerRadius = CornerRadius(radius - innerEdge),
            style = Stroke(2 * innerEdge),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.85f * focus),
            topLeft = Offset(-ring, -ring),
            size = size.copy(width = size.width + 2 * ring, height = size.height + 2 * ring),
            cornerRadius = CornerRadius(radius + ring),
            style = Stroke(2 * ring),
        )
    }
}

/**
 * OK opens the app when it's released after a short press. A long press does nothing.
 */
internal fun Modifier.opensOnPress(onOpen: () -> Unit): Modifier = onKeyEvent { event ->
    val confirm = event.key.nativeKeyCode in setOf(KEYCODE_DPAD_CENTER, KEYCODE_ENTER, KEYCODE_NUMPAD_ENTER)
    if (!confirm) return@onKeyEvent false
    val native = event.nativeKeyEvent
    val shortPress = native.eventTime - native.downTime < ViewConfiguration.getLongPressTimeout()
    if (event.type == KeyEventType.KeyUp && !native.isCanceled && shortPress) {
        onOpen()
    }
    true
}

@Preview(widthDp = 960, heightDp = 160)
@Composable
internal fun DockPreview() {
    Box(modifier = Modifier.background(Color(0xFF101224)).padding(20.dp)) {
        Dock(tiles = HomeState.previewValue.tiles, focused = App.Netflix, onFocus = {}, onOpen = {})
    }
}
