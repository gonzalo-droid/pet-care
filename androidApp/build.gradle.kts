import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

/**
 * Firma de release para la prueba interna de Google Play (docs/BETA.md). El keystore y sus claves
 * nunca van al repositorio (regla 6): se leen de local.properties o de variables de entorno.
 * Sin ellos, el release queda sin firmar y no se puede subir.
 */
val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
fun secret(name: String): String? = localProps.getProperty(name) ?: System.getenv(name)
val releaseStore = secret("BESTID_UPLOAD_STORE_FILE")?.let { rootProject.file(it) }?.takeIf { it.exists() }
if (releaseStore == null) {
    logger.warn("Falta la keystore de subida (BESTID_UPLOAD_*, ver local.properties.example): el release quedará sin firmar.")
}

android {
    namespace = "com.bestid.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.nekodojo.bestid"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        // Una sola fuente de versión (VER-01): gradle/libs.versions.toml.
        versionCode = libs.versions.app.versionCode.get().toInt()
        versionName = libs.versions.app.versionName.get()
    }
    signingConfigs {
        if (releaseStore != null) {
            create("upload") {
                storeFile = releaseStore
                storePassword = secret("BESTID_UPLOAD_STORE_PASSWORD")
                keyAlias = secret("BESTID_UPLOAD_KEY_ALIAS")
                keyPassword = secret("BESTID_UPLOAD_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        release {
            // Sin R8 en la beta: Room, Koin y kotlinx.serialization necesitan reglas que se validan aparte.
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("upload")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.composeApp)
    implementation(projects.core.platform)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
}
