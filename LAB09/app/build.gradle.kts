plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android) // Убрали "jetbrains", обычно в новых проектах так
    kotlin("kapt")
}

android {
    namespace = "com.example.lab09"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.lab09"
        minSdk = 26
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Версии библиотек
    val work_version = "2.9.0"
    val room_version = "2.6.1"
    val retrofit_version = "2.11.0"
    val lifecycle_version = "2.7.0"

    // 1. WorkManager (Фоновые задачи)
    implementation("androidx.work:work-runtime-ktx:$work_version")

    // 2. Room (Локальная база данных)
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    "kapt"("androidx.room:room-compiler:$room_version")

    // 3. Retrofit и Gson (Сетевые запросы и парсинг JSON)
    implementation("com.squareup.retrofit2:retrofit:$retrofit_version")
    implementation("com.squareup.retrofit2:converter-gson:$retrofit_version")

    // 4. Lifecycle (ViewModel и LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")

    // 5. Coroutines (Асинхронное программирование)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
}