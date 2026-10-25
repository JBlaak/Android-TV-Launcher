package dev.jblaak.tvlauncher

import android.service.dreams.DreamService
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dev.jblaak.tvlauncher.home.App
import dev.jblaak.tvlauncher.home.AppBanner
import dev.jblaak.tvlauncher.home.HomeConfig
import dev.jblaak.tvlauncher.home.HomeStateHolder
import dev.jblaak.tvlauncher.home.InstalledApps
import dev.jblaak.tvlauncher.home.LastOpenedApp
import dev.jblaak.tvlauncher.screensaver.Screensaver
import dev.jblaak.tvlauncher.time.SystemClock
import dev.jblaak.tvlauncher.weather.SharedPreferencesLastWeather
import dev.jblaak.tvlauncher.weather.openMeteoWeatherSource
import kotlinx.coroutines.launch

class SkyScreensaver :
    DreamService(),
    LifecycleOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val homeStateHolder by lazy {
        HomeStateHolder(
            config = HomeConfig.Enschede,
            installedApps = object : InstalledApps {
                override fun isInstalled(packageName: String): Boolean = false

                override fun banner(packageName: String): AppBanner? = null
            },
            lastOpenedApp = object : LastOpenedApp {
                override fun get(): App? = null

                override fun set(app: App) = Unit
            },
            clock = SystemClock(),
            weatherSource = openMeteoWeatherSource(),
            lastWeather = SharedPreferencesLastWeather(getSharedPreferences("weather", MODE_PRIVATE)),
            listener = object : HomeStateHolder.Events {
                override fun appLaunchRequested(packageName: String) = Unit

                override fun settingsRequested() = Unit
            },
        )
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch { homeStateHolder.keepWeather() }
                homeStateHolder.keepTime()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        val view = ComposeView(this)
        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)
        view.setContent {
            val state by homeStateHolder.state.collectAsState()
            Screensaver(state = state)
        }
        setContentView(view)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        homeStateHolder.returnHome()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onDreamingStopped() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        super.onDreamingStopped()
    }

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.onDestroy()
    }
}
