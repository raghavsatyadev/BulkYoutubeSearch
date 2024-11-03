import groovy.json.JsonOutput
import java.util.Properties

plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)

    id("kotlin-parcelize")
    id("com.google.devtools.ksp")

    id("androidx.navigation.safeargs")
}

fun readProperties(propertiesFile: File) = Properties().apply {
    propertiesFile.inputStream().use { fis ->
        load(fis)
    }
}

val props = readProperties(file("../secret.properties"))

android {
    namespace = libs.versions.supportId.get()
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles.add(file("consumer-rules.pro"))

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.expandProjection", "true")
        }

        sourceSets {
            getByName("androidTest").assets.srcDirs(files("$projectDir/schemas"))
        }

        ndk { abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a")) }

        val jsonValue = file("../keys.json").readText()
        buildConfigField("String", "youtube_api_keys", JsonOutput.toJson(jsonValue))
    }
    signingConfigs {
        create("release") {
            storeFile = file("../keystore_123456.jks")
            storePassword = "123456"
            keyAlias = "misfit_challenge"
            keyPassword = "123456"
        }
    }
    buildTypes {
        create("beta") {
            isMinifyEnabled = true
            proguardFiles.addAll(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    file("proguard-rules.pro")
                )
            )
            resValue("string", "app_name", libs.versions.betaAppName.get())
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles.addAll(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    file("proguard-rules.pro")
                )
            )
            resValue("string", "app_name", libs.versions.releaseAppName.get())
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            isMinifyEnabled = false
            resValue("string", "app_name", libs.versions.debugAppName.get())
        }
        compileOptions {
            isCoreLibraryDesugaringEnabled = true
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += "-Xjvm-default=all"
        }
        buildFeatures {
            viewBinding = true
            buildConfig = true
            resValues = true
        }
        flavorDimensions.add("isPlayStoreVersion")
        productFlavors {
            create("Prod") {
            }
            create("Dev") {
            }
        }
        androidComponents.beforeVariants { variant ->
            val names = variant.flavorName

            if ((names == "Dev" && variant.buildType == "release") ||
                (names == "Prod" && (variant.buildType != "release"))
            ) {
                variant.enable = false
            }
        }
        packaging {
            jniLibs {
                pickFirsts.addAll(
                    listOf(
                        "lib/*/libc++_shared.so",
                        "lib/*/libgnustl_shared.so",
                        "lib/*/libyuv.so",
                        "lib/*/libopenh264.so"
                    )
                )
            }
        }
    }
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlinx.coroutines.android)

    // Android
    implementation(libs.bundles.androidx)

    // LifeCycle
    implementation(libs.bundles.lifecycle)

    // Navigation
    implementation(libs.bundles.navigation)

    // Retrofit
    implementation(libs.bundles.retrofit)

    // Firebase
    implementation(libs.bundles.firebase)

    // Paging
    implementation(libs.androidx.paging.runtime)

    // Coil
    implementation(libs.bundles.coil)

    // Admob
    implementation(libs.play.services.ads)

    // Room
    implementation(libs.androidx.room)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime)

    // Paging
    implementation(libs.androidx.paging.runtime)

    // Test
    testImplementation(libs.bundles.test)
    androidTestImplementation(libs.bundles.androidTest)

    // Others
    implementation(libs.permissionx)
    implementation(libs.imagepicker)
    implementation(libs.lottie)

    implementation(libs.androidx.multidex)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}