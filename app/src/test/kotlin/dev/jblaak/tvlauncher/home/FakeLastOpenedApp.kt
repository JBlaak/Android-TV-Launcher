package dev.jblaak.tvlauncher.home

internal class FakeLastOpenedApp(
    var app: App? = null,
) : LastOpenedApp {

    override fun get(): App? = app

    override fun set(app: App) {
        this.app = app
    }
}
