plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-parcelize")
}

android {
    namespace = "dev.lackluster.hyperx.compose"
    compileSdk = 36

    defaultConfig {
        minSdk = 31
        consumerProguardFiles("consumer-rules.pro")
    }
    buildFeatures {
        compose = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    kotlin {
        jvmToolchain(21)
    }
}

@Suppress("UseTomlInstead")
dependencies {
    api("top.yukonga.miuix.kmp:miuix:0.8.0-rc06")
    api("top.yukonga.miuix.kmp:miuix-icons-android:0.8.0-rc06")
    api("top.yukonga.miuix.kmp:miuix-navigation3-ui-android:0.8.0-rc06-patch01")
    api("dev.chrisbanes.haze:haze:1.7.1")
    api("androidx.compose.foundation:foundation:1.10.1")
    api("androidx.activity:activity-compose:1.12.2")
    api("androidx.navigation3:navigation3-runtime:1.1.0-alpha02")
    api("androidx.navigationevent:navigationevent-compose:1.0.1")
    api("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    api("androidx.lifecycle:lifecycle-viewmodel-navigation3:2.10.0")
    implementation("com.mocharealm.gaze:capsule:2.1.1-patch2")
    implementation("io.github.biezhi:TinyPinyin:2.0.3.RELEASE")
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
}