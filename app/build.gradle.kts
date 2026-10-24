plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Release signing comes from the environment. CI sets these from repository secrets.
val keystore = System.getenv("DETENT_KEYSTORE")?.let { file(it) }?.takeIf { it.exists() }
// Without the release keystore, a release build is signed with the debug key, which is public. That is fine
// for a local install or a pull-request build, but such an APK must never be published. The build warns here,
// and the release workflow fails a tag without the keystore and checks the key of the APK that it publishes.
val signedRelease = keystore != null
if (!signedRelease && gradle.startParameter.taskNames.any { it.contains("Release") }) {
    logger.warn("Detent: DETENT_KEYSTORE is not set. The release APK is signed with the debug key and must not be published.")
}

android {
    namespace = "app.detent.launcher"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.detent.launcher"
        // Predefined vibration effects and the home role need API 29.
        minSdk = 29
        targetSdk = 36
        // CI passes the tag as -PdetentVersion and the run number as -PdetentCode.
        versionCode = providers.gradleProperty("detentCode").orNull?.toInt() ?: 1
        versionName = providers.gradleProperty("detentVersion").orNull ?: "0.1.0"
    }

    signingConfigs {
        if (signedRelease) {
            create("release") {
                storeFile = keystore
                storePassword = System.getenv("DETENT_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("DETENT_KEY_ALIAS")
                keyPassword = System.getenv("DETENT_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // A release without DETENT_KEYSTORE uses the local debug key so that `adb install` still works.
            // The warning above and the release workflow keep a debug-signed APK out of a published release.
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    androidResources {
        // The wallpapers are already compressed WebP files.
        noCompress += "webp"
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)

    testImplementation(libs.junit)
}
