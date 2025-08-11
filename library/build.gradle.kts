import java.util.*

plugins {
  alias(libs.plugins.android.library)
  id("maven-publish")
  id("signing")
  id("com.gradleup.nmcp")
}

val artifactName = rootProject.name

group = rootProject.group
version = rootProject.version

// Load local publish/signing properties from publish.properties if present
val publishPropertiesFile = rootProject.file("publish.properties")
if (publishPropertiesFile.exists()) {
  val localProperties = Properties().apply {
    publishPropertiesFile.inputStream().use { this.load(it) }
  }
  for ((keyAny, valueAny) in localProperties) {
    val key = keyAny as String
    val value = valueAny as String
    if (key == "signing.secretKeyRingFile") {
      project.extensions.extraProperties.set(key, rootProject.file(value).absolutePath)
    } else {
      project.extensions.extraProperties.set(key, value)
    }
  }
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
  }
}

android {
  namespace = "io.michaelrocks.libphonenumber.android"

  defaultConfig {
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    minSdk = libs.versions.minSdk.get().toInt()
  }

  compileOptions {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaLibrary.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaLibrary.get())
    isCoreLibraryDesugaringEnabled = false
  }

  buildTypes {
    release {
      isMinifyEnabled = false
    }
  }

  publishing {
    singleVariant("release") {
      withSourcesJar()
      withJavadocJar()
    }
  }

  lint {
    abortOnError = false
    targetSdk = libs.versions.targetSdk.get().toInt()
  }

  testOptions {
    targetSdk = libs.versions.targetSdk.get().toInt()
  }
}

androidComponents {
  beforeVariants(selector().all()) { variant ->
    if (variant.buildType != "release") {
      variant.enable = false
    }
  }
}

dependencies {
  testImplementation(libs.junit)
  testImplementation(libs.mockito.core)
}

// Move publishing and signing after evaluation so that the 'release' component exists
afterEvaluate {
  publishing {
    publications {
      create<MavenPublication>("release") {
        from(components["release"])
        artifactId = artifactName
        pom {
          name.set("libphonenumber-android")
          description.set("An Android port of Google's libphonenumber.")
          inceptionYear.set("2016")
          url.set("https://github.com/michaelrocks/libphonenumber-android")
          packaging = "aar"

          licenses {
            license {
              name.set("The Apache License, Version 2.0")
              url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
              distribution.set("repo")
            }
          }
          developers {
            developer {
              id.set("MichaelRocks")
              name.set("Michael Rozumyanskiy")
              email.set("michael.rozumyanskiy@gmail.com")
            }
          }
          scm {
            connection.set("scm:git:git://github.com/michaelrocks/libphonenumber-android.git")
            developerConnection.set("scm:git:ssh://git@github.com/michaelrocks/libphonenumber-android.git")
            url.set("https://github.com/michaelrocks/libphonenumber-android")
          }
        }
      }
    }
  }

  signing {
    sign(publishing.publications["release"])
  }
}
