plugins {
    alias(libs.plugins.bestid.kmp.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

kotlin {
    // JVM solo para probar Room en memoria sin Android (jvmTest).
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.core.domain)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.kotlinx.serialization.json)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        jvmTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.androidx.room.testing)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    listOf("kspAndroid", "kspIosArm64", "kspIosSimulatorArm64", "kspJvm").forEach {
        add(it, libs.androidx.room.compiler)
    }
}
