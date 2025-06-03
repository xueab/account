plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.accountbook"
    compileSdk = 35

    packagingOptions {
        // 解决方案1: 排除特定的冲突文件
        exclude("META-INF/versions/9/OSGI-INF/MANIFEST.MF")

        // 或者解决方案2: 合并冲突文件
        // merge("META-INF/versions/9/OSGI-INF/MANIFEST.MF")

        // 或者解决方案3: 更精确地排除特定库的冲突文件
        // exclude("META-INF/versions/9/OSGI-INF/MANIFEST.MF" from "bcprov-jdk18on-1.78.1.jar")
        // exclude("META-INF/versions/9/OSGI-INF/MANIFEST.MF" from "jspecify-1.0.0.jar")
    }

    defaultConfig {
        applicationId = "com.example.accountbook"
        minSdk = 21
        targetSdk = 35
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
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
//    implementation(libs.lifecycle.livedata.ktx)
//    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // 阿里云OCR
    implementation("com.aliyun:ocr_api20210707:3.1.3") {
        exclude(group = "com.google.code.findbugs", module = "jsr305")
    }

    // MPAndroidChart
    implementation(libs.mpandroidchart) {
        exclude(module = "support-annotations")
    }
    implementation("de.hdodenhof:circleimageview:3.1.0")
}