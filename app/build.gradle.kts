plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.skin_melanoma_mobile_scanning_application"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.skin_melanoma_mobile_scanning_application"
        minSdk = 24
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.firebase.firestore.ktx)
    // TensorFlow Lite
    implementation(libs.tensorflow.tensorflow.lite.v2140)

    configurations.all {
        exclude(group = "com.google.ai.edge.litert", module = "litert-api")
        exclude(group = "com.google.ai.edge.litert", module = "litert")
        exclude(group = "com.google.ai.edge.litert", module = "litert-support-api")
    }

    // Camera dependencies
    implementation(libs.camera.camera2.v131)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.camera.view.v131)

    implementation(libs.coil.compose)  // For basic Coil-Compose integration
    implementation(libs.coil)         // For core Coil functionality


    implementation(libs.litert)
    implementation(libs.litert.support.api)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.room.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.material3)

    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth.ktx)
}
android {
    // ... other android configurations ...

    packagingOptions {
        pickFirst("lib/arm64-v8a/libtensorflowlite_jni.so")
        pickFirst("lib/armeabi-v7a/libtensorflowlite_jni.so")
        pickFirst("lib/x86/libtensorflowlite_jni.so")
        pickFirst("lib/x86_64/libtensorflowlite_jni.so")
    }
}