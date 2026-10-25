package dev.jblaak.tvlauncher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.jblaak.tvlauncher.apps.PackageManagerInstalledApps
import dev.jblaak.tvlauncher.apps.SharedPreferencesLastOpenedApp
import dev.jblaak.tvlauncher.apps.buildInstalledApps
import dev.jblaak.tvlauncher.home.Home
import dev.jblaak.tvlauncher.home.HomeConfig
import dev.jblaak.tvlauncher.home.HomeStateHolder
import dev.jblaak.tvlauncher.time.SystemClock
import kotlinx.coroutines.launch

class HomeActivity : ComponentActivity() {

    private val installedApps by lazy { PackageManagerInstalledApps(packageManager) }

    private val homeStateHolder by lazy {
        HomeStateHolder(
            config = HomeConfig.Enschede,
            installedApps = buildInstalledApps(installedApps),
            lastOpenedApp = SharedPreferencesLastOpenedApp(getSharedPreferences("home", Context.MODE_PRIVATE)),
            clock = SystemClock(),
            listener = object : HomeStateHolder.Events {
                override fun appLaunchRequested(packageName: String) {
                    installedApps.launchIntent(packageName)?.let { startActivity(it) }
                }

                override fun settingsRequested() {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this) {}
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) { homeStateHolder.keepTime() }
        }
        setContent {
            val state by homeStateHolder.state.collectAsState()
            Home(
                state = state,
                onFocus = homeStateHolder::focus,
                onOpen = homeStateHolder::open,
                onFocusSettings = homeStateHolder::focusSettings,
                onLeaveSettings = homeStateHolder::leaveSettings,
                onOpenSettings = homeStateHolder::openSettings,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        homeStateHolder.returnHome()
    }
}
