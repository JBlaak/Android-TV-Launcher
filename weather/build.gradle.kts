plugins {
    id("tvlauncher.kotlin.library")
    alias(libs.plugins.jetbrainsKotlinSerialization)
}

dependencies {
    api(projects.sky)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.serialization.kotlinx.json)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)
}
