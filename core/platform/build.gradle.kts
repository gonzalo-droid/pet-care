plugins {
    alias(libs.plugins.bestid.kmp.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
        }
    }
}
