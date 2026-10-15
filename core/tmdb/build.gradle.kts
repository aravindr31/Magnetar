plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.northlight.core.tmdb"
    compileSdk = 34

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // TMDB API key from local.properties
        buildConfigField(
            "String",
            "TMDB_API_KEY",
            "\"${project.findProperty("TMDB_API_KEY") ?: ""}\""
        )
    }

    buildFeatures {
        buildConfig = true
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
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.networking)
    implementation(libs.hilt.android)
}
