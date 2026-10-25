package dev.jblaak.tvlauncher

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.jblaak.tvlauncher.apps.PackageManagerInstalledApps
import dev.jblaak.tvlauncher.apps.SharedPreferencesLastOpenedApp
import dev.jblaak.tvlauncher.apps.buildInstalledApps
import dev.jblaak.tvlauncher.home.Home
import dev.jblaak.tvlauncher.home.HomeConfig
import dev.jblaak.tvlauncher.home.HomeStateHolder

class HomeActivity : ComponentActivity() {

    private val installedApps by lazy { PackageManagerInstalledApps(packageManager) }

    private val homeStateHolder by lazy {
        HomeStateHolder(
            config = HomeConfig.Enschede,
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
        onBackPressedDispatcher.addCallback(this) {}
        setContent {
            val state by homeStateHolder.state.collectAsState()
            Home(state = state, onFocus = homeStateHolder::focus, onOpen = homeStateHolder::open)
        }
    }

    override fun onResume() {
        super.onResume()
        homeStateHolder.returnHome()
    }
}
