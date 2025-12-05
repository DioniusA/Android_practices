plugins {
    alias(libs.plugins.android.test)
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.example.practice_9.baselineprofile"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation("androidx.benchmark:benchmark-macro-junit4:1.3.3")
    implementation("androidx.test.uiautomator:uiautomator:2.3.0")
}

