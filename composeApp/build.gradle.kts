plugins {
    alias(libs.plugins.bestid.kmp.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.model)
            implementation(projects.core.domain)
            implementation(projects.core.data)
            implementation(projects.core.ui)
            implementation(projects.core.platform)
            implementation(projects.core.designsystem)
            implementation(projects.core.navigation)
            implementation(projects.feature.home)
            implementation(projects.feature.cartilla)
            implementation(projects.feature.health)
            implementation(projects.feature.pet)
            implementation(projects.feature.onboarding)
            implementation(projects.feature.share)
            implementation(projects.feature.settings)

            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.coil.compose)
            implementation(libs.okio)
        }
        commonTest.dependencies {
            implementation(libs.koin.test)
        }
        iosTest.dependencies {
            implementation(libs.compose.ui.test)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.datastore.preferences)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.koin.test)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.datastore.preferences)
        }
    }
}

compose.resources {
    packageOfResClass = "com.bestid.app.resources"
}

// Las pruebas de UI en el simulador de iOS guardan capturas aquí (el CI las sube como artefacto).
tasks.withType<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest>().configureEach {
    environment("SIMCTL_CHILD_BESTID_SCREENSHOTS", layout.buildDirectory.dir("ui-screenshots").get().asFile.absolutePath)
}

// VER-02: la versión de iOS es una copia de la del catálogo; check falla si se desincroniza.
val checkIosVersion by tasks.registering {
    val xcconfig = rootProject.file("iosApp/Configuration/Config.xcconfig")
    val name = libs.versions.app.versionName.get()
    val code = libs.versions.app.versionCode.get()
    inputs.file(xcconfig)
    doLast {
        // Compara clave por clave: buscar "MARKETING_VERSION=2.0.2" en el texto aceptaría "2.0.25".
        val values = xcconfig.readLines().mapNotNull { line ->
            line.substringBefore("//").split("=", limit = 2).takeIf { it.size == 2 }?.let { it[0].trim() to it[1].trim() }
        }.toMap()
        check(values["MARKETING_VERSION"] == name && values["CURRENT_PROJECT_VERSION"] == code) {
            "Config.xcconfig (${values["MARKETING_VERSION"]} / ${values["CURRENT_PROJECT_VERSION"]}) no coincide con " +
                "libs.versions.toml ($name / $code)."
        }
    }
}
tasks.named("check") { dependsOn(checkIosVersion) }
