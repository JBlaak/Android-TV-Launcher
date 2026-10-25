package dev.jblaak.tvlauncher.home

internal class RecordingEvents : HomeStateHolder.Events {

    var launchedPackages = emptyList<String>()
        private set

    override fun appLaunchRequested(packageName: String) {
        launchedPackages = launchedPackages + packageName
    }
}
