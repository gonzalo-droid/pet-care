import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Módulo KMP con Compose Multiplatform y Compose Resources.
 * La clase `Res` es pública y vive en `<namespace>.resources` para poder usarse desde otros módulos.
 */
class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("bestid.kmp.library")
        pluginManager.apply("org.jetbrains.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<KotlinMultiplatformExtension> {
            (this as org.gradle.api.plugins.ExtensionAware).extensions
                .getByType<KotlinMultiplatformAndroidLibraryTarget>()
                .androidResources { enable = true }

            sourceSets.getByName("commonMain").dependencies {
                implementation(libs.library("compose-runtime"))
                implementation(libs.library("compose-foundation"))
                implementation(libs.library("compose-ui"))
                implementation(libs.library("compose-material3"))
                implementation(libs.library("compose-components-resources"))
                implementation(libs.library("compose-ui-tooling-preview"))
            }
        }

        (extensions.getByType<ComposeExtension>() as org.gradle.api.plugins.ExtensionAware)
            .extensions.configure<ResourcesExtension> {
                publicResClass = true
                packageOfResClass = "$bestIdNamespace.resources"
                generateResClass = org.jetbrains.compose.resources.ResourcesExtension.ResourceClassGeneration.Always
            }

        dependencies {
            add("androidRuntimeClasspath", libs.library("compose-ui-tooling"))
        }
    }
}
