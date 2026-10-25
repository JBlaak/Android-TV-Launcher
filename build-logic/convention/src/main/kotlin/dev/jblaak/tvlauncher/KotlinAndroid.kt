package dev.jblaak.tvlauncher

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension) {
    commonExtension.apply {
        compileSdk = compileSdkVersion

        defaultConfig.minSdk = minSdkVersion

        compileOptions.sourceCompatibility = JavaVersion.VERSION_17
        compileOptions.targetCompatibility = JavaVersion.VERSION_17
    }

    configureKotlin()
}

internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    configureKotlin()
}

private fun Project.configureKotlin() {
    tasks.withType(KotlinCompile::class.java) {
        compilerOptions {
            val allWarningsAsErrors = project.findProperty("allWarningsAsErrors") as String?
            this@compilerOptions.allWarningsAsErrors = allWarningsAsErrors.toBoolean()

            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}
