plugins {
  alias(libs.plugins.android.application)
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
  }
}

android {
  namespace = "io.michaelrocks.libphonenumber.android.sample"
  compileSdk = libs.versions.compileSdk.get().toInt()
  buildToolsVersion = libs.versions.buildTools.get()

  defaultConfig {
    minSdk = libs.versions.minSdkSample.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()

    applicationId = "io.michaelrocks.libphonenumber.android.sample"

    versionCode = 1
    versionName = rootProject.version.toString()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    debug {
      matchingFallbacks += listOf("release")
    }

    release {
      isMinifyEnabled = true
    }
  }

  lint {
    abortOnError = false
  }

  signingConfigs {
    getByName("debug") {
      storeFile = rootProject.file("debug.keystore")
    }
  }

  testOptions {
    val minSdk = libs.versions.minSdkSample.get().toInt()
    val targetSdk = libs.versions.targetSdk.get().toInt()
    managedDevices {
      localDevices {
        create("pixel2Api$minSdk") {
          device = "Pixel 2"
          apiLevel = minSdk
          systemImageSource = "aosp"
        }
        create("pixel2Api$targetSdk") {
          device = "Pixel 2"
          apiLevel = targetSdk
          systemImageSource = "aosp"
        }
        create("atdApi$minSdk") {
          device = "Pixel 2"
          apiLevel = minSdk
          // ATD image doesn't exist for API level 21
          systemImageSource = "aosp"
        }
        create("atdApi$targetSdk") {
          device = "Pixel 2"
          apiLevel = targetSdk
          systemImageSource = "aosp-atd"
        }
      }
      groups {
        register("localDevices") {
          targetDevices.add(devices.getByName("pixel2Api$minSdk"))
          targetDevices.add(devices.getByName("pixel2Api$targetSdk"))
        }
        register("ciDevices") {
          targetDevices.add(devices.getByName("atdApi$minSdk"))
          targetDevices.add(devices.getByName("atdApi$targetSdk"))
        }
      }
    }
  }
}

dependencies {
  implementation(project(":library"))

  androidTestImplementation(libs.androidx.test.espresso.core)
  androidTestImplementation(libs.androidx.test.rules)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.ext.junit)
}
