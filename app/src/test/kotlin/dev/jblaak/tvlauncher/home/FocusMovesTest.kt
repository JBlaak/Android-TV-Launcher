package dev.jblaak.tvlauncher.home

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class FocusMovesTest {

    @Test
    fun `each app has its own logo move`() {
        /* When */
        val moves = App.entries.associateWith { it.logoMove() }

        /* Then */
        assertEquals(
            mapOf(
                App.NpoTv to LogoMove.Wobble,
                App.Netflix to LogoMove.TaDum,
                App.DisneyPlus to LogoMove.Sparkle,
                App.Plex to LogoMove.SlideIn,
                App.YouTube to LogoMove.Nudge,
            ),
            moves,
        )
    }

    @Test
    fun `the npo logo tips left then right`() {
        /* When */
        val left = LogoMove.Wobble.poseAt(225)
        val right = LogoMove.Wobble.poseAt(675)

        /* Then */
        assertEquals(-9f, left.rotation, 0.01f)
        assertEquals(9f, right.rotation, 0.01f)
    }

    @Test
    fun `the npo logo wobbles twice`() {
        /* When */
        val secondLeft = LogoMove.Wobble.poseAt(900 + 225)

        /* Then */
        assertEquals(-9f, secondLeft.rotation, 0.01f)
    }

    @Test
    fun `the netflix logo swells then settles back`() {
        /* When */
        val swell = LogoMove.TaDum.poseAt(165)
        val dip = LogoMove.TaDum.poseAt(302)

        /* Then */
        assertEquals(1.2f, swell.scale, 0.001f)
        assertEquals(0.96f, dip.scale, 0.001f)
    }

    @Test
    fun `the youtube logo nudges forward`() {
        /* When */
        val pose = LogoMove.Nudge.poseAt(200)

        /* Then */
        assertEquals(5.25.dp, pose.offsetX)
    }

    @Test
    fun `the plex logo slides in from the left while it fades in`() {
        /* When */
        val pose = LogoMove.SlideIn.poseAt(0)

        /* Then */
        assertEquals(LogoPose(rotation = 0f, scale = 1f, offsetX = (-10.5).dp, alpha = 0.3f), pose)
    }

    @Test
    fun `the disney plus logo itself stays still`() {
        /* When */
        val pose = LogoMove.Sparkle.poseAt(300)

        /* Then */
        assertEquals(LogoPose.Rest, pose)
    }

    @Test
    fun `every logo move ends at rest`() {
        /* When */
        val poses = LogoMove.entries.map { it.poseAt(it.durationMillis) }

        /* Then */
        assertEquals(LogoMove.entries.map { LogoPose.Rest }, poses)
    }

    @Test
    fun `the tile dips then overshoots before it settles`() {
        /* When */
        val dip = tileBounceAt(0)
        val overshoot = tileBounceAt(270)
        val settled = tileBounceAt(TileBounceMillis)

        /* Then */
        assertEquals(0.94f, dip, 0.001f)
        assertEquals(1.05f, overshoot, 0.001f)
        assertEquals(1f, settled, 0.001f)
    }
}
