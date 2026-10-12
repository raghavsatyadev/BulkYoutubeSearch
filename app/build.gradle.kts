import com.android.build.api.artifact.SingleArtifact
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import java.text.SimpleDateFormat
import java.util.Date

plugins {
  alias(libs.plugins.android.application)

  alias(libs.plugins.google.gms)

  alias(libs.plugins.ksp)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.parcelize)
  alias(libs.plugins.kotlin.compose)

  id("com.google.firebase.crashlytics")
}

sonar {
  setAndroidVariant("DevDebug")
  properties {
    property(
      "sonar.androidLint.reportPaths",
      "${layout.buildDirectory.asFile}/reports/lint-results-DevDebug.xml",
    )
  }
}

kotlin { jvmToolchain(21) }

android {
  namespace = libs.versions.nameSpace.get()
  compileSdk { version = release(libs.versions.compileSdk.get().toInt()) { minorApiLevel = 1 } }
  // compileSdkPreview = libs.versions.compileSdkPreview.get()
  buildToolsVersion = libs.versions.buildTools.get()

  defaultConfig {
    applicationId = libs.versions.appIdDev.get()
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()
    // targetSdkPreview = libs.versions.targetSdkPreview.get()
    versionCode = libs.versions.versionCode.get().toInt()
    versionName = libs.versions.versionName.get()
    multiDexEnabled = true

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  signingConfigs {
    create("release") {
      storeFile = file("../keystore_123456.jks")
      storePassword = "123456"
      keyAlias = "bys"
      keyPassword = "123456"
    }
  }
  buildTypes {
    create("beta") {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles.addAll(
        listOf(getDefaultProguardFile("proguard-android-optimize.txt"), file("proguard-rules.pro"))
      )
      signingConfig = signingConfigs.getByName("release")
      //            applicationIdSuffix = ".beta"
    }
    getByName("release") {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles.addAll(
        listOf(getDefaultProguardFile("proguard-android-optimize.txt"), file("proguard-rules.pro"))
      )
      signingConfig = signingConfigs.getByName("release")

      ndk {
        abiFilters.clear()
        abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
      }
    }
    getByName("debug") {
      isMinifyEnabled = false
      isShrinkResources = false
      //            applicationIdSuffix = ".debug"
      configure<CrashlyticsExtension> { mappingFileUploadEnabled = false }
    }
  }
  compileOptions { isCoreLibraryDesugaringEnabled = true }
  buildFeatures {
    compose = true
    buildConfig = true
    resValues = true
  }

  flavorDimensions.add("isPlayStoreVersion")
  productFlavors {
    create("Prod") {
      dimension = "isPlayStoreVersion"
      applicationId = libs.versions.appIdProd.get()
    }
    create("Dev") {
      dimension = "isPlayStoreVersion"
      applicationId = libs.versions.appIdDev.get()
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

  // Copies release APK, mapping file and AAB to build/outputs/apk with a timestamped name.
  onVariants(selector().withBuildType("release")) { variant ->
    val variantNameCapitalized = variant.name.replaceFirstChar { it.uppercase() }
    val outputFullName = getBuildName(variant.name)
    val buildOutputDirectory = layout.buildDirectory.dir("outputs/apk")

    val copyApkTask =
      tasks.register<Copy>("copy${variantNameCapitalized}Apk") {
        from(variant.artifacts.get(SingleArtifact.APK)) {
          include("*.apk")
          rename { "$outputFullName.apk" }
        }
        from(variant.artifacts.get(SingleArtifact.OBFUSCATION_MAPPING_FILE)) {
          rename { "$outputFullName.txt" }
        }
        into(buildOutputDirectory)
      }
    val copyBundleTask =
      tasks.register<Copy>("copy${variantNameCapitalized}Bundle") {
        from(variant.artifacts.get(SingleArtifact.BUNDLE)) { rename { "$outputFullName.aab" } }
        into(buildOutputDirectory)
      }

    tasks.configureEach {
      when (name) {
        "assemble$variantNameCapitalized" -> finalizedBy(copyApkTask)
        "bundle$variantNameCapitalized" -> finalizedBy(copyBundleTask)
      }
    }
  }
}

dependencies {
  implementation(project(path = ":support"))

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

  // Compose
  implementation(libs.bundles.compose)
  debugImplementation(libs.compose.ui.tooling)

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

fun getBuildName(variantName: String): String {
  val timestamp = SimpleDateFormat("dd-MM-yy_HH-mm").format(Date())

  return "${libs.versions.apkName.get()}-${variantName}-${timestamp}"
}
