// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.dependencyanalysis) apply false
}

buildscript {
    dependencies {
        classpath(libs.licenses.plugin)
        classpath("gradle.plugin.chrisney:enigma:1.0.0.8")
    }

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        google()
        gradlePluginPortal()
    }
}

tasks.register("clean", Delete::class){
    delete(rootProject.buildDir)
}