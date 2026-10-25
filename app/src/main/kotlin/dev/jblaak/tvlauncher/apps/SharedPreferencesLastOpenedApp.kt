package dev.jblaak.tvlauncher.apps

import android.content.SharedPreferences
import androidx.core.content.edit
import dev.jblaak.tvlauncher.home.App
import dev.jblaak.tvlauncher.home.LastOpenedApp

internal class SharedPreferencesLastOpenedApp(
    private val preferences: SharedPreferences,
) : LastOpenedApp {

    override fun get(): App? {
        val name = preferences.getString(KEY, null)
        return App.entries.firstOrNull { it.name == name }
    }

    override fun set(app: App) {
        preferences.edit { putString(KEY, app.name) }
    }

    private companion object {
        const val KEY = "last_opened_app"
    }
}
