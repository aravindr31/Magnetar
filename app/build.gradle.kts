plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.northlight"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.northlight"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
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
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:ui"))
    implementation(project(":core:yts"))
    implementation(project(":core:tmdb"))
    implementation(project(":core:streaming"))

    // Streaming implementations
    implementation(project(":streaming:torrent"))
    implementation(project(":streaming:realdebrid"))
    implementation(project(":streaming:seedr"))

    // Feature modules
    implementation(project(":feature:browse"))
    implementation(project(":feature:details"))
    implementation(project(":feature:player"))
    implementation(project(":feature:settings"))

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime)

    // Coil
    implementation(libs.coil.compose)
}
