plugins {
    alias(libs.plugins.bestid.kmp.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.coil.compose)
            implementation(libs.androidx.navigationevent.compose)
        }
    }
}
