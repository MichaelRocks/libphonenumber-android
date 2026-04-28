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

  compileSdk {
    version = release(libs.versions.compileSdk.get().toInt())
  }

  buildToolsVersion = libs.versions.buildTools.get()

  defaultConfig {
    minSdk = libs.versions.minSdkSample.get().toInt()

    targetSdk {
      version = release(libs.versions.targetSdk.get().toInt())
    }

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
      isShrinkResources = true
    }
  }

  lint {
    abortOnError = false
  }

  testOptions {
    @Suppress("UnstableApiUsage")
    managedDevices {
      localDevices {
        create("pixel2Api30") {
          device = "Pixel 2"
          apiLevel = 30
          systemImageSource = "aosp-atd"
        }
      }
    }
  }

  signingConfigs {
    getByName("debug") {
      storeFile = rootProject.file("debug.keystore")
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
