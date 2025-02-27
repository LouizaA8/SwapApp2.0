plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)

}

android {
    namespace = "com.example.swapapp20"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.swapapp20"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"



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
        viewBinding = true
    }

}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    implementation(libs.firebase.auth)
    implementation(libs.legacy.support.v4)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.viewpager2)
    implementation (libs.firebase.auth.v2130)
    implementation (libs.firebase.database)
    implementation (libs.imagepicker)
    implementation (libs.play.services.maps)
    implementation (libs.play.services.location)
    implementation(libs.places)
    implementation (libs.places.v330)
    implementation (libs.play.services.maps.v1810)
   implementation (libs.firebase.firestore)
    implementation (libs.firebase.storage)
    implementation (libs.cloudinary.android)
    implementation (libs.glide)
    annotationProcessor (libs.compiler)
    implementation (libs.cardstackview)
    implementation (libs.material.v150)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation (libs.circleimageview)
}}
