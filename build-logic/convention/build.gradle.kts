import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kotlinLibrary") {
            id = "tvlauncher.kotlin.library"
            implementationClass = "KotlinLibraryConventionPlugin"
        }

        register("androidApplication") {
            id = "tvlauncher.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
    }
}
