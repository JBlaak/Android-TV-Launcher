pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "Android-TV-Launcher"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")
include(":sky")
include(":weather")
