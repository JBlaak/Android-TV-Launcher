plugins {
    id("tvlauncher.android.application")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.paparazzi)
}

android {
    namespace = "dev.jblaak.tvlauncher"

    defaultConfig {
        applicationId = "dev.jblaak.tvlauncher"
    }
}

dependencies {
    implementation(projects.sky)
    implementation(projects.weather)

    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)

    testImplementation(libs.paparazzi)
    testImplementation(libs.googleTestParameterInjector)
}
