import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.HostTestBuilder
import dev.jblaak.tvlauncher.configureAndroidCompose
import dev.jblaak.tvlauncher.configureKotlinAndroid
import dev.jblaak.tvlauncher.targetSdkVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.kotlin
import java.io.File
import java.util.Properties

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
            }

            val versionNameProvider = providers
                .gradleProperty("versionName")
                .orElse("development")

            val versionCodeProvider = providers
                .gradleProperty("versionCode")
                .orElse("1")

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)

                defaultConfig {
                    targetSdk = target.targetSdkVersion
                    versionName = versionNameProvider.get()
                    versionCode = versionCodeProvider.get().toInt()
                }

                buildTypes {
                    getByName("debug") {
                        versionNameSuffix = "-dev"
                    }
                    release {
                        isMinifyEnabled = true
                        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                    }
                }

                val signing = releaseSigningProperties()
                if (signing != null) {
                    signingConfigs {
                        create("release") {
                            storeFile = file(signing.getProperty("storeFile"))
                            storePassword = signing.getProperty("storePassword")
                            keyAlias = signing.getProperty("keyAlias")
                            keyPassword = signing.getProperty("keyPassword")
                        }
                    }

                    buildTypes {
                        release {
                            signingConfig = signingConfigs.getByName("release")
                        }
                    }
                }

                lint {
                    checkAllWarnings = true
                    warningsAsErrors = true

                    disable.add("SyntheticAccessor")
                    disable.add("ObsoleteLintCustomCheck")
                    disable.add("GradleDependency")
                    disable.add("AndroidGradlePluginVersion")
                    disable.add("NewerVersionAvailable")
                }
            }

            dependencies {
                add("testImplementation", kotlin("test-junit"))
            }

            extensions.configure<ApplicationAndroidComponentsExtension> {
                enableUnitTestsOnReleaseOnly()
            }

            configureAndroidCompose(extensions.getByType<ApplicationExtension>())
        }
    }

    private fun ApplicationAndroidComponentsExtension.enableUnitTestsOnReleaseOnly() {
        beforeVariants { variant ->
            variant.hostTests[HostTestBuilder.UNIT_TEST_TYPE]?.enable = variant.buildType == "release"
        }
    }

    private fun Project.releaseSigningProperties(): Properties? {
        val path = providers.gradleProperty("tvlauncher.signing")
            .orElse(File(System.getProperty("user.home"), ".android-tv-launcher/signing.properties").path)
            .get()
        val contents = providers.fileContents(layout.projectDirectory.file(path)).asText.orNull
        if (contents == null) {
            logger.warn("No release signing properties at $path, so release builds are unsigned. See the README.")
            return null
        }
        return Properties().apply { load(contents.reader()) }
    }
}
