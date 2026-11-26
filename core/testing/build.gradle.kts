plugins {
    alias(libs.plugins.bestid.kmp.library)
}

// Fakes de los puertos del dominio para las pruebas de core/domain y de los ViewModels de feature/* (TST-04, TST-05).
// Solo se usa desde commonTest.
kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.core.domain)
        }
    }
}
