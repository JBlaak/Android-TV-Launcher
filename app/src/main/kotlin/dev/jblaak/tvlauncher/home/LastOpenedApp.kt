package dev.jblaak.tvlauncher.home

internal interface LastOpenedApp {
    fun get(): App?

    fun set(app: App)
}
