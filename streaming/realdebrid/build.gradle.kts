plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.northlight.streaming.realdebrid"
    compileSdk = 35

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:streaming"))
    implementation(project(":core:network"))
    implementation(libs.security.crypto)
    implementation(libs.bundles.coroutines)
    implementation(libs.okhttp.core)
    implementation(libs.hilt.android)
}
