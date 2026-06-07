plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.room")
    id("com.google.devtools.ksp")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        // =============================================
        // commonMain — 跨平台共享代码
        // =============================================
        commonMain.dependencies {
            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // Lifecycle (JetBrains KMP 适配版)
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

            // Navigation (JetBrains 跨平台移植版 — androidMain 和 iosMain 通用)
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")

            // Room KMP (实体 + DAO 接口)
            implementation("androidx.room:room-runtime:2.7.0")
            implementation("androidx.room:room-ktx:2.7.0")

            // kotlinx-datetime — 跨平台日期时间（替代 java.util.Calendar）
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
        }

        // =============================================
        // androidMain — Android 平台专有
        // =============================================
        androidMain.dependencies {
            // Android Activity 入口
            implementation("androidx.activity:activity-compose:1.10.1")
            implementation("androidx.core:core-ktx:1.15.0")
        }

        // =============================================
        // iosMain — iOS 平台专有
        // =============================================
        iosMain.dependencies {
            // iOS 数据库驱动 — 必须用 bundled 版本（自带 SQLite），
            // sqlite-framework 是 Android 专有
            implementation("androidx.sqlite:sqlite-bundled:2.5.1")
        }
    }
}

// =============================================
// Android 配置 — 保留原项目所有设置
// =============================================
android {
    namespace = "com.example.accountingapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.accountingapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 5
        versionName = "1.3.1"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../.signing/signing-key.p12")
            storePassword = "accounting123"
            keyAlias = "accounting-key"
            keyPassword = "accounting123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isDebuggable = true  // 方便备份恢复数据
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// =============================================
// Room KSP — 为每个 target 注册 Room 编译器
// =============================================
dependencies {
    add("kspAndroid", "androidx.room:room-compiler:2.7.0")
    add("kspIosX64", "androidx.room:room-compiler:2.7.0")
    add("kspIosArm64", "androidx.room:room-compiler:2.7.0")
    add("kspIosSimulatorArm64", "androidx.room:room-compiler:2.7.0")
}
