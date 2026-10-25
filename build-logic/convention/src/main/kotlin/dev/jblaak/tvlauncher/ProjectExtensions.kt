package dev.jblaak.tvlauncher

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal val Project.compileSdkVersion: Int
    get() = libs.findVersion("compileSdk").get().requiredVersion.toInt()

internal val Project.minSdkVersion: Int
    get() = libs.findVersion("minSdk").get().requiredVersion.toInt()

internal val Project.targetSdkVersion: Int
    get() = libs.findVersion("targetSdk").get().requiredVersion.toInt()
