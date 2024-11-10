import groovy.json.JsonOutput
import java.util.Properties

plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)

    id("kotlin-parcelize")
    id("com.google.devtools.ksp")

    id("androidx.navigation.safeargs")
    alias(libs.plugins.room)
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

    room {
        schemaDirectory("$projectDir/schemas")
        // incremental("true")
        // generateKotlin("true")
    }
    ksp {
        arg("room.incremental", "true")
        arg("room.generateKotlin", "true")
    }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles.add(file("consumer-rules.pro"))

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
    implementation(libs.bundles.kotlin)

    // Android
    implementation(libs.bundles.androidx)

    // LifeCycle
    implementation(libs.bundles.lifecycle)

    // Navigation
    implementation(libs.bundles.navigation)

    // Firebase
    implementation(libs.bundles.firebase)

    // Coil
    implementation(libs.bundles.coil)

    // Ktor
    implementation(libs.bundles.ktor)

    // Admob
    implementation(libs.play.services.ads)

    // Room
    implementation(libs.androidx.room)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime)

    // Test
    testImplementation(libs.bundles.test)
    androidTestImplementation(libs.bundles.androidTest)

    // Others
    implementation(libs.bundles.other)

    implementation(libs.androidx.multidex)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}