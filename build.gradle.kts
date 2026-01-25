import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-parcelize")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("21")
    }
}

android {
    namespace = "dev.lackluster.hyperx.compose"
    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
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
}

@Suppress("UseTomlInstead")
dependencies {
    api("top.yukonga.miuix.kmp:miuix:0.7.2")
    api("dev.chrisbanes.haze:haze:1.7.1")
    api("androidx.compose.foundation:foundation:1.10.2")
    api("androidx.activity:activity-compose:1.12.3")
    api("androidx.navigation:navigation-compose:2.9.7")
    api("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("com.mocharealm.gaze:capsule:2.1.1-patch2")
    implementation("com.github.promeg:tinypinyin:2.0.3") // maven("https://maven.aliyun.com/repository/public")
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
}