plugins {
    alias(libs.plugins.bestid.kmp.library)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
        }
    }
}
