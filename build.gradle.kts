// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.apply {
        set("compose_version", "1.8.2")
    }
    repositories {
        maven { url = uri("https://maven.localazy.com/repository/release/") }
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.google.com/")
            name = "Google"
        }
    }
    dependencies {
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.1.21-2.0.1")
        classpath("com.android.tools.build:gradle:8.10.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.21")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.56.2")
//        classpath("com.localazy:gradle:2.0.2")
//        classpath("com.google.gms:google-services:4.4.2")
//        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.3")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}