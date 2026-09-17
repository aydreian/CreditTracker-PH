import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.hilt)
  alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.credittrackph"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.credittrackph"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        
        val localProps = Properties()
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) {
            localProps.load(localPropsFile.inputStream())
        }
        val groqApiKey = localProps.getProperty("groq.api.key") ?: System.getenv("GROQ_API_KEY") ?: "YOUR_GROQ_API_KEY_HERE"
        buildConfigField("String", "GROQ_API_KEY", "\"$groqApiKey\"")

        // Room schema export
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("release.keystore")
            storePassword = "password"
            keyAlias = "release_key"
            keyPassword = "password"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      buildConfig = true
    }
    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(libs.kotlinx.coroutines.android)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose UI
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.icons.extended)
  debugImplementation(libs.androidx.compose.ui.tooling)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // Room + SQLCipher (encrypted DB)
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  ksp(libs.androidx.room.compiler)
  implementation(libs.sqlcipher.android)
  implementation(libs.androidx.sqlite)

  // Hilt DI
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)
  implementation(libs.hilt.navigation.compose)
  implementation(libs.hilt.work)
  ksp(libs.hilt.work.compiler)

  // WorkManager
  implementation(libs.androidx.work.runtime.ktx)

  // Glance Widgets
  implementation(libs.androidx.glance.appwidget)
  implementation(libs.androidx.glance.material3)

  // Retrofit + OkHttp (Groq API)
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.converter.gson)
  implementation(libs.okhttp.core)
  implementation(libs.okhttp.logging)

  // Biometric
  implementation(libs.androidx.biometric)

  // Vico Charts
  implementation(libs.vico.compose.m3)

  // DataStore
  implementation(libs.androidx.datastore.preferences)

  // Serialization
  implementation(libs.kotlinx.serialization.json)

  // Tests
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}
