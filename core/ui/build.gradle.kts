plugins {
    alias(libs.plugins.bestid.kmp.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(projects.core.designsystem)
        }
    }
}
