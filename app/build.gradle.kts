plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.qzz.demo2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.qzz.demo2"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

//        multiDexEnabled = true

        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
//        debug {
//            isMinifyEnabled = false
//            applicationIdSuffix = ".debug"
//            versionNameSuffix = "-debug"
//        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        aidl = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}