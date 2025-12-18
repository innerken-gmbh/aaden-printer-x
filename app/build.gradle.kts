plugins {
    id("com.android.application")
    id("kotlin-android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.21"
    // this version matches your Kotlin version
    //id("com.google.gms.google-services")
    //id("com.google.devtools.ksp")
}

//apply(plugin = "com.google.gms.google-services")  // Google Services plugin
//apply(plugin = "com.google.firebase.crashlytics")

android {
    namespace = "com.innerken.aadenprinterx"
    compileSdk = 36

    signingConfigs {
        create("InnerKenJks") {
            enableV1Signing = true
            enableV2Signing = true
            storeFile = file("innerken.jks")
            storePassword = "asd123456"
            keyAlias = "key0"
            keyPassword = "asd123456"
        }
    }

    defaultConfig {
        applicationId = "com.innerken.aadenprinterx"
        minSdk = 26
        targetSdk = 36
        versionCode = 12
        versionName = "1.0.10"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
       signingConfig = signingConfigs.getByName("InnerKenJks")
    }

    buildTypes {
        release {
           signingConfig = signingConfigs.getByName("InnerKenJks")
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "CLOUD_URL", "\"https://cloud-v2.aaden.io\"")
        }
        debug {
            signingConfig = signingConfigs.getByName("InnerKenJks")
            buildConfigField("String", "CLOUD_URL", "\"https://cloud-v2.aaden.io\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {

    implementation("androidx.window:window:1.4.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.ui:ui:1.8.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.8.2")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.lifecycle:lifecycle-service:2.9.0")

    debugImplementation("androidx.compose.ui:ui-tooling:1.8.2")
    implementation("androidx.preference:preference-ktx:1.2.1")
    //implementation(files("libs/scale-service-part-lib1.2.jar"))
    implementation("androidx.activity:activity-compose:1.10.1")

    implementation("nl.jacobras:Human-Readable:1.11.0")

    kapt("com.google.dagger:hilt-compiler:2.56.2")
    implementation("com.google.dagger:hilt-android:2.56.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")

    implementation("androidx.navigation:navigation-compose:2.9.0")
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    // Architectural Components
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    //商米打印库
    implementation("com.sunmi:printerx:1.0.18")

    // dialog
    implementation("com.afollestad.material-dialogs:core:3.3.0")
    implementation("com.afollestad.material-dialogs:datetime:3.3.0")
    implementation("com.afollestad.material-dialogs:input:3.3.0")
    implementation("com.afollestad.material-dialogs:color:3.3.0")

    //Outter Dependency, Not important
    implementation("io.coil-kt:coil-compose:2.7.0")

    // google accompanist
    val accompanistVersion = "0.36.0"
    implementation("com.google.accompanist:accompanist-placeholder-material:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-flowlayout:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-pager:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-webview:$accompanistVersion")
    //implementation("com.sumup:merchant-sdk:4.2.0")

    implementation("androidx.savedstate:savedstate-ktx:1.3.0")
    implementation("com.github.haroldadmin:NetworkResponseAdapter:5.0.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.21")

//    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))
//    implementation("com.google.firebase:firebase-crashlytics-ktx")
//    implementation("com.google.firebase:firebase-analytics-ktx")
   // implementation("com.github.DantSu:ESCPOS-ThermalPrinter-Android:3.2.0")
    implementation("network.chaintech:qr-kit:3.0.7")
//    implementation("cafe.adriel.lyricist:lyricist:1.7.0")
//    ksp("cafe.adriel.lyricist:lyricist-processor:1.7.0")
    implementation("org.jsoup:jsoup:1.20.1")
    implementation("com.github.davidmoten:word-wrap:0.1.13")
    implementation("ru.gildor.coroutines:kotlin-coroutines-okhttp:1.0")

    //implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.3")
    implementation("com.github.jiusetian:EasySocket:2.11")

    implementation("androidx.webkit:webkit:1.13.0")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.8.2")
//    implementation("com.google.firebase:firebase-auth-ktx")
//    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("io.ktor:ktor-network:3.1.3")
    //implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.materialkolor:material-kolor:2.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")

    // Lottie for animations
    implementation("com.airbnb.android:lottie-compose:6.6.6")
}

kapt {
    correctErrorTypes = true
}
//
//ksp {
//    arg("incremental", "false")
//    arg("ksp.skipAllChecks", "true")
//}