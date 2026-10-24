plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.sblorftz.timer"
    compileSdk = 35

    defaultConfig {
        // Package nuovo rispetto alla versione Flutter (com.sblorftz.sblorftz_timer):
        // le due app convivono finché la nuova non la sostituisce del tutto.
        applicationId = "com.sblorftz.timer"
        minSdk = 23
        targetSdk = 35
        versionCode = 33
        versionName = "2.0-beta9"
    }

    signingConfigs {
        create("upload") {
            // Chiave fissa dai secret della CI (copia cifrata nel vault). In locale non c'è
            // e la release si firma con la chiave debug: APK installabile, ma non aggiornabile
            // sopra quelli della CI.
            val storePath = providers.environmentVariable("KEYSTORE_PATH").orNull
            if (storePath != null) {
                storeFile = file(storePath)
                // keytool di JDK 9+ scrive PKCS12 qualunque sia l'estensione.
                storeType = "PKCS12"
                storePassword = providers.environmentVariable("KEYSTORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (System.getenv("KEYSTORE_PATH") != null) {
                signingConfigs.getByName("upload")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.zxing.core)
}
