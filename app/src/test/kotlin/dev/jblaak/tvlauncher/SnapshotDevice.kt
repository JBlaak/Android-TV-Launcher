package dev.jblaak.tvlauncher

import app.cash.paparazzi.DeviceConfig
import com.android.resources.Density
import com.android.resources.Keyboard
import com.android.resources.KeyboardState
import com.android.resources.Navigation
import com.android.resources.ScreenOrientation
import com.android.resources.ScreenRatio
import com.android.resources.ScreenSize
import com.android.resources.TouchScreen
import com.android.resources.UiMode

/**
 * The Philips TV: 1920×1080 at density 320, so Compose lays out a 960×540 dp canvas.
 */
internal val Television = DeviceConfig(
    screenHeight = 1080,
    screenWidth = 1920,
    xdpi = 320,
    ydpi = 320,
    orientation = ScreenOrientation.LANDSCAPE,
    uiMode = UiMode.TELEVISION,
    density = Density.XHIGH,
    ratio = ScreenRatio.LONG,
    size = ScreenSize.LARGE,
    keyboard = Keyboard.NOKEY,
    touchScreen = TouchScreen.NOTOUCH,
    keyboardState = KeyboardState.HIDDEN,
    softButtons = false,
    navigation = Navigation.DPAD,
    released = "2021",
)

internal enum class Locale(val tag: String) {
    English("en"),
    Dutch("nl"),
}
