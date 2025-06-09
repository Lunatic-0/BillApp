plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.bill"
    compileSdk = 35

    defaultConfig {
        resConfigs( "zh")
        applicationId = "com.example.bill"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 支持多 Dex（可选）
        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true                   // 启用混淆压缩
            isShrinkResources = true                 // 启用资源压缩
            isDebuggable = false                     // 关闭调试信息
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

    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.fragment)
    implementation(libs.recyclerview)
    implementation(libs.gson)
    implementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
