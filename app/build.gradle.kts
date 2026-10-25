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
    implementation(libs.androidx.core.ktx)
    implementation(libs.compose.animation)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.paparazzi)
    testImplementation(libs.googleTestParameterInjector)
    testImplementation(libs.kotlinx.coroutines.test)
}
