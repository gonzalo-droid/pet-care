plugins {
    alias(libs.plugins.bestid.kmp.library)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}
