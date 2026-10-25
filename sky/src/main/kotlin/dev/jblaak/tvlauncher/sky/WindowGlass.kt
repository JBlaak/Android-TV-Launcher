package dev.jblaak.tvlauncher.sky

/**
 * The window pane itself, closest to the viewer: rain [beads] on the glass, or [frost] creeping in from its corners
 * (canvas copy, DockNight glass). Positions and sizes are in reference pixels.
 */
data class WindowGlass(
    val beads: List<Bead>,
    val frost: Boolean,
) {
    companion object {
        val Clean = WindowGlass(beads = emptyList(), frost = false)
    }
}

/**
 * A raindrop sitting on the glass in the box at [topLeft]. A bead with a [slide] slides down the pane now and then.
 */
data class Bead(
    val topLeft: ScreenPoint,
    val width: Double,
    val height: Double,
    val slide: Slide?,
)

/**
 * A bead's loop of [periodSeconds], running `phaseSeconds` ahead: it holds still for the first half, creeps 40 px
 * down, then speeds up and slides 820 px down and off the pane.
 */
data class Slide(
    val periodSeconds: Double,
    val phaseSeconds: Double,
)
