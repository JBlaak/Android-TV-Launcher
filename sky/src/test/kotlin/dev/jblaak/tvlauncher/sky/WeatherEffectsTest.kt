package dev.jblaak.tvlauncher.sky

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WeatherEffectsTest {

    @Test
    fun `a south-west wind of 20 km per hour leans the rain about 24 degrees to the right`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Rain, Wind(from = 225.0, speed = 20.0, gusts = 30.0))

        /* Then */
        assertEquals(23.8, scene.effects.rain.first().tilt, 0.1)
    }

    @Test
    fun `a south-west wind moves the rain to the right`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Rain, Wind(from = 225.0, speed = 20.0, gusts = 30.0))

        /* Then */
        assertTrue(scene.effects.rain.all { it.drift > 0 })
    }

    @Test
    fun `a north-east wind leans the rain the other way`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Rain, Wind(from = 45.0, speed = 20.0, gusts = 30.0))

        /* Then */
        assertEquals(-23.8, scene.effects.rain.first().tilt, 0.1)
    }

    @Test
    fun `a west wind blows straight at the window, so the rain falls straight down`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Rain, Wind(from = 270.0, speed = 40.0, gusts = 60.0))

        /* Then */
        assertTrue(scene.effects.rain.all { it.tilt == 0.0 && it.drift == 0.0 })
    }

    @Test
    fun `an east wind blows straight away from the window, so the rain falls straight down`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Rain, Wind(from = 90.0, speed = 40.0, gusts = 60.0))

        /* Then */
        assertEquals(0.0, scene.effects.rain.first().tilt, 1e-9)
    }

    @Test
    fun `the rain leans at most 55 degrees`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Rain, Wind(from = 180.0, speed = 200.0, gusts = 250.0))

        /* Then */
        assertEquals(55.0, scene.effects.rain.first().tilt, 1e-9)
    }

    @Test
    fun `rain falls in 54 drops`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Rain, rainWind)

        /* Then */
        assertEquals(54, scene.effects.rain.size)
    }

    @Test
    fun `rain drifts under five clouds`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Rain, rainWind)

        /* Then */
        assertEquals(5, scene.effects.clouds.size)
    }

    @Test
    fun `a storm falls in 90 drops`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Storm, stormWind)

        /* Then */
        assertEquals(90, scene.effects.rain.size)
    }

    @Test
    fun `in a storm every third drop rides the gusts at one and a half times the wind`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Storm, Wind(from = 225.0, speed = 20.0, gusts = 30.0))

        /* Then */
        assertEquals(List(30) { 33.5 }, scene.effects.rain.filterIndexed { i, _ -> i % 3 == 0 }.map { (it.tilt * 10).toInt() / 10.0 })
    }

    @Test
    fun `in a storm the other drops lean with the wind`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Storm, Wind(from = 225.0, speed = 20.0, gusts = 30.0))

        /* Then */
        assertEquals(23.8, scene.effects.rain[1].tilt, 0.1)
    }

    @Test
    fun `plain rain has no gusty drops`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Rain, Wind(from = 225.0, speed = 20.0, gusts = 30.0))

        /* Then */
        assertEquals(1, scene.effects.rain.map { it.tilt }.distinct().size)
    }

    @Test
    fun `nine dark clouds race through a storm`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Storm, stormWind)

        /* Then */
        assertEquals(9, scene.effects.scud.size)
    }

    @Test
    fun `storm clouds race to the right in a south-west wind`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Storm, stormWind)

        /* Then */
        assertTrue(scene.effects.scud.all { it.toX > it.fromX })
    }

    @Test
    fun `storm clouds race to the left in a north-east wind`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Storm, stormWind.copy(from = 45.0))

        /* Then */
        assertTrue(scene.effects.scud.all { it.toX < it.fromX })
    }

    @Test
    fun `storm clouds race faster in a stronger wind`() {
        /* When */
        val gale = skyScene(evening, westWindow, SkyWeather.Storm, stormWind.copy(speed = 100.0))
        val breeze = skyScene(evening, westWindow, SkyWeather.Storm, stormWind.copy(speed = 40.0))

        /* Then */
        assertTrue(gale.effects.scud.first().periodSeconds < breeze.effects.scud.first().periodSeconds)
    }

    @Test
    fun `three rain curtains sweep through a storm`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Storm, stormWind)

        /* Then */
        assertEquals(3, scene.effects.curtains.size)
    }

    @Test
    fun `rain curtains slant like the drops`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Storm, Wind(from = 225.0, speed = 20.0, gusts = 30.0))

        /* Then */
        assertEquals(23.8, scene.effects.curtains.first().skew, 0.1)
    }

    @Test
    fun `seven leaves tumble past when the storm blows across the view at more than 20 km per hour`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Storm, Wind(from = 180.0, speed = 21.0, gusts = 30.0))

        /* Then */
        assertEquals(7, scene.effects.leaves.size)
    }

    @Test
    fun `no leaves fly when the storm blows across the view at 20 km per hour`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Storm, Wind(from = 180.0, speed = 20.0, gusts = 30.0))

        /* Then */
        assertTrue(scene.effects.leaves.isEmpty())
    }

    @Test
    fun `no leaves fly when a strong storm blows straight at the window`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Storm, Wind(from = 270.0, speed = 90.0, gusts = 120.0))

        /* Then */
        assertTrue(scene.effects.leaves.isEmpty())
    }

    @Test
    fun `leaves fly in from the right in a north-east wind`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Storm, stormWind.copy(from = 45.0))

        /* Then */
        assertTrue(scene.effects.leaves.all { it.direction == -1 && it.start.x == 1280.0 })
    }

    @Test
    fun `lightning flashes in a storm`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Storm, stormWind)

        /* Then */
        assertTrue(scene.effects.lightning)
    }

    @Test
    fun `there is no lightning in the rain`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Rain, rainWind)

        /* Then */
        assertFalse(scene.effects.lightning)
    }

    @Test
    fun `snow falls in 48 flakes`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Snow, snowWind)

        /* Then */
        assertEquals(48, scene.effects.snow.size)
    }

    @Test
    fun `snow drifts six pixels per km per hour of sideways wind`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Snow, Wind(from = 180.0, speed = 15.0, gusts = 20.0))

        /* Then */
        assertEquals(90.0, scene.effects.snow.first().drift, 1e-9)
    }

    @Test
    fun `cloudy weather drifts seven clouds`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Clouds, calmWind)

        /* Then */
        assertEquals(7, scene.effects.clouds.size)
    }

    @Test
    fun `fog drifts in five bands`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Fog, calmWind)

        /* Then */
        assertEquals(5, scene.effects.clouds.size)
    }

    @Test
    fun `warm weather shimmers with heat haze`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Warm, calmWind)

        /* Then */
        assertTrue(scene.effects.heatHaze)
    }

    @Test
    fun `a clear sky has no heat haze`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Clear, calmWind)

        /* Then */
        assertFalse(scene.effects.heatHaze)
    }

    @Test
    fun `a clear night shows 42 stars`() {
        /* When */
        val scene = skyScene(night, westWindow, SkyWeather.Clear, calmWind)

        /* Then */
        assertEquals(42, scene.effects.stars.size)
    }

    @Test
    fun `a warm night shows stars too`() {
        /* When */
        val scene = skyScene(night, westWindow, SkyWeather.Warm, calmWind)

        /* Then */
        assertEquals(42, scene.effects.stars.size)
    }

    @Test
    fun `the stars wait until the sun is 6 degrees below the horizon`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Clear, calmWind)

        /* Then */
        assertTrue(scene.effects.stars.isEmpty())
    }

    @Test
    fun `clouds hide the stars`() {
        /* When */
        val scene = skyScene(night, westWindow, SkyWeather.Clouds, calmWind)

        /* Then */
        assertTrue(scene.effects.stars.isEmpty())
    }

    @Test
    fun `a clear evening has no weather effects`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Clear, calmWind)

        /* Then */
        assertEquals(WeatherEffects.None, scene.effects)
    }

    @Test
    fun `without live wind a storm blows from the south-west at 75 km per hour`() {
        /* When */
        val scene = skyScene(evening, westWindow, SkyWeather.Storm)

        /* Then */
        assertEquals(skyScene(evening, westWindow, SkyWeather.Storm, Wind(from = 225.0, speed = 75.0, gusts = 75.0)), scene)
    }

    @Test
    fun `without live wind rain blows from the south-west at 20 km per hour`() {
        /* When */
        val wind = typicalWind(SkyWeather.Rain)

        /* Then */
        assertEquals(225.0 to 20.0, wind.from to wind.speed)
    }

    @Test
    fun `without live wind snow blows from the east at 15 km per hour`() {
        /* When */
        val wind = typicalWind(SkyWeather.Snow)

        /* Then */
        assertEquals(90.0 to 15.0, wind.from to wind.speed)
    }

    @Test
    fun `without live wind other weather blows from the south-west at 12 km per hour`() {
        /* When */
        val wind = typicalWind(SkyWeather.Fog)

        /* Then */
        assertEquals(225.0 to 12.0, wind.from to wind.speed)
    }

    private companion object {
        val westWindow = Window(location = Location(latitude = 52.2215, longitude = 6.8937), facing = 270.0)
        val evening: Instant = ZonedDateTime.of(2026, 10, 25, 17, 5, 0, 0, ZoneId.of("Europe/Amsterdam")).toInstant()
        val night: Instant = ZonedDateTime.of(2026, 10, 25, 23, 0, 0, 0, ZoneId.of("Europe/Amsterdam")).toInstant()
        val calmWind = Wind(from = 225.0, speed = 12.0, gusts = 12.0)
        val rainWind = Wind(from = 225.0, speed = 20.0, gusts = 20.0)
        val stormWind = Wind(from = 225.0, speed = 75.0, gusts = 75.0)
        val snowWind = Wind(from = 90.0, speed = 15.0, gusts = 15.0)
    }
}
