plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Injected by GitHub Actions (-PversionName, -PversionCode, -PgithubRepo); sane defaults locally.
val appVersionName = (findProperty("versionName") as String?) ?: "1.0.0"
val appVersionCode = ((findProperty("versionCode") as String?) ?: "1").toInt()
val githubRepo = (findProperty("githubRepo") as String?) ?: "YOUR_USER/CubiTV"

android {
    namespace = "com.cubitv.app"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.cubitv.app"
        minSdk = 23
        targetSdk = 34
        versionCode = appVersionCode
        versionName = appVersionName
        buildConfigField("String", "GITHUB_REPO", "\"$githubRepo\"")
    }
    signingConfigs {
        create("release") {
            // Same key every build = updates install over the old version.
            // Override with env vars / GitHub secrets for a private key (see README).
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "../keystore/cubitv.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "cubitv123"
            keyAlias = System.getenv("KEY_ALIAS") ?: "cubitv"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "cubitv123"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.tv:tv-material:1.0.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
