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

android {
    namespace = "com.bestid.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.bestid.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        // Sube versionCode en cada subida a Google Play: ./gradlew :androidApp:bundleRelease -Pbestid.versionCode=2
        versionCode = (findProperty("bestid.versionCode") as String?)?.toInt() ?: 1
        versionName = "0.5.0"
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
