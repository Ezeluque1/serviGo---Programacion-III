plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.servigov2"
    // compileSdk 37: lo piden androidx.core 1.19.0 y lifecycle 2.11.0, que ya
    // estaban en el proyecto. targetSdk sigue en 36 (son cosas distintas).
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.servigov2"
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
        // ViewBinding: genera una clase por layout para acceder a las vistas
        // sin usar findViewById.
        viewBinding = true
    }
}

dependencies {
    // --- Vistas XML: Toolbar, RecyclerView y BottomNavigationView ---
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.material)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // --- Room: base de datos local ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // --- Retrofit + Gson: llamadas a la API ---
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // --- Corrutinas (para no trabajar en el hilo principal) ---
    implementation(libs.kotlinx.coroutines.android)

    // --- Glide: carga de imagenes desde internet ---
    implementation(libs.glide)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}