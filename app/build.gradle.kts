@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget()

    compilerOptions {
        optIn.set(
            listOf(
                "androidx.compose.material3.ExperimentalMaterial3Api",
                "androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
                "androidx.compose.foundation.layout.ExperimentalLayoutApi",
                "androidx.compose.foundation.ExperimentalFoundationApi",
            )
        )
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.androidx.compose.ui)
            implementation(libs.androidx.compose.ui.graphics)
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.compose.material3)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.androidx.adaptive.navigation3)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.coil.compose)
        }

        androidMain.dependencies {
            implementation(libs.materialKolor)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.viewmodel.ktx)
            implementation(libs.pinyin4j)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
        }

        invokeWhenCreated("androidDebug") {
            dependencies {
                implementation(libs.androidx.compose.ui.tooling)
                implementation(libs.androidx.compose.ui.test.manifest)
            }
        }

        androidUnitTest.dependencies {
            implementation(libs.junit)
        }

        androidInstrumentedTest.dependencies {
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.androidx.junit)
            implementation(libs.androidx.espresso.core)
            implementation(libs.androidx.compose.ui.test.junit4)
        }
    }
}
android {
    namespace = "io.github.sanlorng.warmtones"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "io.github.sanlorng.warmtones"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}
