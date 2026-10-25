package dev.jblaak.tvlauncher.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.node.requireLayoutCoordinates
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.jblaak.tvlauncher.sky.Sun
import kotlin.math.ceil

/**
 * Frosted glass: the sky behind this element, softly blurred, drawn under the element's own tint. API 31 and up get a
 * real blur of [radius]. The Android 11 TV gets the sky drawn a second time at ⅛ resolution and stretched back up, a
 * cheap blur that stays in step with the animating sky (docs/adr/0001-glass-blur.md). Outside a [SkyBackground] it
 * draws nothing.
 */
internal fun Modifier.frostedGlass(shape: Shape, radius: Dp): Modifier = this then FrostedGlassElement(shape, radius)

private data class FrostedGlassElement(val shape: Shape, val radius: Dp) : ModifierNodeElement<FrostedGlassNode>() {
    override fun create() = FrostedGlassNode(shape, radius)

    override fun update(node: FrostedGlassNode) {
        node.shape = shape
        node.radius = radius
        node.invalidateDraw()
    }
}

private class FrostedGlassNode(
    var shape: Shape,
    var radius: Dp,
) : Modifier.Node(),
    DrawModifierNode,
    GlobalPositionAwareModifierNode,
    CompositionLocalConsumerModifierNode {

    private var position = Offset.Unspecified
    private var lowResolution: LowResolutionSky? = null
    private var blurLayer: GraphicsLayer? = null

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val current = coordinates.positionInRoot()
        if (current != position) {
            position = current
            invalidateDraw()
        }
    }

    override fun ContentDrawScope.draw() {
        val backdrop = currentValueOf(LocalSkyBackdrop)
        if (backdrop != null) {
            val coordinates = requireLayoutCoordinates()
            val window = SkyWindow(coordinates.positionInRoot(), coordinates.findRootCoordinates().size.width / 1280f)
            val outline = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@draw)) }
            clipPath(outline) {
                when (currentValueOf(LocalRendering)) {
                    Rendering.Api30 -> drawLowResolution(backdrop, window)
                    Rendering.Api31 -> drawBlurred(backdrop, window)
                }
            }
        }
        drawContent()
    }

    private fun DrawScope.drawLowResolution(backdrop: SkyBackdrop, window: SkyWindow) {
        val pixels = IntSize(ceil(size.width / 8).toInt().coerceAtLeast(1), ceil(size.height / 8).toInt().coerceAtLeast(1))
        val sky = lowResolution?.takeIf { it.size == pixels } ?: LowResolutionSky(pixels).also { lowResolution = it }
        sky.scope.draw(this, layoutDirection, sky.canvas, Size(pixels.width.toFloat(), pixels.height.toFloat())) {
            drawRect(Color.Transparent, blendMode = BlendMode.Clear)
            scale(1 / 8f, pivot = Offset.Zero) { drawSky(backdrop, window) }
        }
        drawImage(sky.image, dstSize = IntSize(pixels.width * 8, pixels.height * 8), filterQuality = FilterQuality.Low)
    }

    private fun DrawScope.drawBlurred(backdrop: SkyBackdrop, window: SkyWindow) {
        val layer = blurLayer ?: requireGraphicsContext().createGraphicsLayer().also { blurLayer = it }
        val blur = radius.toPx()
        layer.renderEffect = BlurEffect(blur, blur, TileMode.Clamp)
        layer.record(this, layoutDirection, IntSize(ceil(size.width).toInt(), ceil(size.height).toInt())) {
            drawSky(backdrop, window)
        }
        drawLayer(layer)
    }

    override fun onDetach() {
        blurLayer?.let { requireGraphicsContext().releaseGraphicsLayer(it) }
        blurLayer = null
        lowResolution = null
    }
}

/**
 * Where this element sits on the sky: its top-left corner in screen pixels, and screen pixels per reference pixel.
 */
private class SkyWindow(val position: Offset, val scale: Float)

private class LowResolutionSky(val size: IntSize) {
    val image = ImageBitmap(size.width, size.height)
    val canvas = Canvas(image)
    val scope = CanvasDrawScope()
}

private fun DrawScope.drawSky(backdrop: SkyBackdrop, window: SkyWindow) {
    translate(-window.position.x, -window.position.y) {
        scale(window.scale, pivot = Offset.Zero) {
            with(backdrop) { drawInReferencePixels() }
        }
    }
}

@Preview(device = "id:tv_1080p")
@Composable
internal fun FrostedGlassPreview() {
    val scene = checkNotNull(HomeState.previewValue.sky)
    val sun = (scene.sun as Sun.Disc).position
    SkyBackground(scene = scene, celebration = null, appGlow = Color(0xFFE50914), modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset((sun.x * 0.75 - 150).dp, (sun.y * 0.75 - 30).dp)
                .size(300.dp, 60.dp)
                .frostedGlass(RoundedCornerShape(30.dp), radius = 39.dp)
                .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(30.dp))
                .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(30.dp)),
        )
        Box(
            modifier = Modifier
                .offset((sun.x * 0.75 + 60).dp, (sun.y * 0.75 - 120).dp)
                .size(37.5.dp)
                .frostedGlass(CircleShape, radius = 26.dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                .border(0.75.dp, Color.White.copy(alpha = 0.16f), CircleShape),
        )
    }
}
