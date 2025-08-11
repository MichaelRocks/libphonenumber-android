plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
}

group = "io.michaelrocks"
version = "9.0.5"

tasks.register<Delete>("clean") {
  delete(rootProject.layout.buildDirectory)
}

allprojects {
  val publishPropertiesFile = rootProject.file("publish.properties")
  if (publishPropertiesFile.exists()) {
    val localProperties = java.util.Properties().apply {
      publishPropertiesFile.inputStream().use { load(it) }
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
}
