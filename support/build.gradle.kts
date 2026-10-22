import com.android.build.api.variant.BuildConfigField
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.util.Properties

plugins {
  alias(libs.plugins.android.library)

  alias(libs.plugins.ksp)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.parcelize)

  id("androidx.navigation.safeargs.kotlin")

  alias(libs.plugins.room)
}

fun readProperties(propertiesFile: File) =
  Properties().apply {
    if (propertiesFile.exists()) propertiesFile.inputStream().use { fis -> load(fis) }
  }

val props = readProperties(file("../secret.properties"))

kotlin { jvmToolchain(21) }

room {
  schemaDirectory("$projectDir/schemas")
  // incremental("true")
  // generateKotlin("true")
}

ksp {
  arg("room.incremental", "true")
  arg("room.generateKotlin", "true")
}

android {
  namespace = libs.versions.supportId.get()
  compileSdk = libs.versions.compileSdk.get().toInt()
  // compileSdkPreview = libs.versions.compileSdkPreview.get()
  buildToolsVersion = libs.versions.buildTools.get()

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()
    multiDexEnabled = true

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles.add(file("consumer-rules.pro"))

    ndk { abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a")) }
  }
  sourceSets { getByName("androidTest").assets.directories.add("$projectDir/schemas") }
  signingConfigs {
    create("release") {
      storeFile = file("../keystore_123456.jks")
      storePassword = props.getProperty("storePassword", "")
      keyAlias = "misfit_challenge"
      keyPassword = props.getProperty("keyPassword", "")
    }
  }
  buildTypes {
    create("beta") {
      isMinifyEnabled = true
      proguardFiles.addAll(
        listOf(getDefaultProguardFile("proguard-android-optimize.txt"), file("proguard-rules.pro"))
      )
      resValue("string", "app_name", libs.versions.betaAppName.get())
      signingConfig = signingConfigs.getByName("release")
    }
    getByName("release") {
      isMinifyEnabled = true
      proguardFiles.addAll(
        listOf(getDefaultProguardFile("proguard-android-optimize.txt"), file("proguard-rules.pro"))
      )
      resValue("string", "app_name", libs.versions.releaseAppName.get())
      signingConfig = signingConfigs.getByName("release")
    }
    getByName("debug") {
      isMinifyEnabled = false
      resValue("string", "app_name", libs.versions.debugAppName.get())
    }
  }
  compileOptions { isCoreLibraryDesugaringEnabled = true }
  buildFeatures {
    viewBinding = true
    buildConfig = true
    resValues = true
  }
  flavorDimensions.add("isPlayStoreVersion")
  productFlavors {
    create("Prod") {}
    create("Dev") {}
  }
  packaging {
    jniLibs {
      pickFirsts.addAll(
        listOf(
          "lib/*/libc++_shared.so",
          "lib/*/libgnustl_shared.so",
          "lib/*/libyuv.so",
          "lib/*/libopenh264.so",
        )
      )
    }
  }
}

androidComponents {
  beforeVariants { variant ->
    val names = variant.flavorName

    if (
      (names == "Dev" && variant.buildType == "release") ||
        (names == "Prod" && (variant.buildType != "release"))
    ) {
      variant.enable = false
    }
  }

  onVariants { variant ->
    val file = file("../keys.json")
    var jsonValue = "[]"
    if (file.exists()) {
      // Ship only name + key; keys.json also holds OAuth refresh tokens that must stay off the APK.
      @Suppress("UNCHECKED_CAST")
      val entries = JsonSlurper().parseText(file.readText()) as List<Map<String, Any?>>
      jsonValue = JsonOutput.toJson(entries.map { mapOf("name" to it["name"], "key" to it["key"]) })
    }
    variant.buildConfigFields?.put(
      "youtube_api_keys",
      BuildConfigField("String", JsonOutput.toJson(jsonValue), null),
    )
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

  // Google
  implementation(libs.bundles.google)

  // Coil
  implementation(libs.bundles.coil)

  // Ktor
  implementation(libs.bundles.ktor)

  // Admob
  implementation(libs.play.services.ads)

  // Room
  implementation(libs.room)
  implementation(libs.room.runtime)
  ksp(libs.room.compiler)

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
