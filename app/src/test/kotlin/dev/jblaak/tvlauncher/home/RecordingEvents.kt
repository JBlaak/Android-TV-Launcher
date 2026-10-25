package dev.jblaak.tvlauncher.home

internal class RecordingEvents : HomeStateHolder.Events {

    var launchedPackages = emptyList<String>()
        private set

    var settingsRequests = 0
        private set

    override fun appLaunchRequested(packageName: String) {
        launchedPackages = launchedPackages + packageName
    }

    override fun settingsRequested() {
        settingsRequests++
    }
}
