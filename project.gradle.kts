buildscript {
    dependencies {
        classpath(libs.com.google.gms)
        classpath(libs.com.google.firebase)
        classpath(libs.androidx.navigation)
    }
}
plugins {
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.com.android.library) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.com.google.devtools.ksp) apply false
    alias(libs.plugins.room) apply false

    alias(libs.plugins.org.sonarqube)
}