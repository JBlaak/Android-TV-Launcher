package dev.jblaak.tvlauncher

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.jblaak.tvlauncher.apps.PackageManagerInstalledApps
import dev.jblaak.tvlauncher.apps.SharedPreferencesLastOpenedApp
import dev.jblaak.tvlauncher.apps.buildClock
import dev.jblaak.tvlauncher.apps.buildInstalledApps
import dev.jblaak.tvlauncher.apps.buildRendering
import dev.jblaak.tvlauncher.home.Home
import dev.jblaak.tvlauncher.home.HomeConfig
import dev.jblaak.tvlauncher.home.HomeStateHolder
import dev.jblaak.tvlauncher.home.LocalRendering
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class HomeActivity : ComponentActivity() {

    private val installedApps by lazy { PackageManagerInstalledApps(packageManager) }

    private val homeStateHolder by lazy {
        HomeStateHolder(
            config = HomeConfig.Enschede,
            clock = buildClock(intent),
            installedApps = buildInstalledApps(installedApps),
            lastOpenedApp = SharedPreferencesLastOpenedApp(getSharedPreferences("home", Context.MODE_PRIVATE)),
            listener = object : HomeStateHolder.Events {
                override fun appLaunchRequested(packageName: String) {
                    installedApps.launchIntent(packageName)?.let { startActivity(it) }
                }
            },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rendering = buildRendering(intent)
        onBackPressedDispatcher.addCallback(this) {}
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    delay(30.seconds)
                    homeStateHolder.timePassed()
                }
            }
        }
        setContent {
            val state by homeStateHolder.state.collectAsState()
            CompositionLocalProvider(LocalRendering provides rendering) {
                Home(state = state, onFocus = homeStateHolder::focus, onOpen = homeStateHolder::open)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        homeStateHolder.returnHome()
    }
}
