package io.michaelrocks.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.Properties

class PublishPropertiesPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val publishPropertiesFile = project.rootProject.file("publish.properties")
    if (!publishPropertiesFile.exists()) return

    val properties = Properties().apply {
      publishPropertiesFile.inputStream().use { load(it) }
    }

    for ((rawKey, rawValue) in properties) {
      val key = rawKey as String
      val value = rawValue as String
      if (key == "signing.secretKeyRingFile") {
        project.extensions.extraProperties.set(key, project.rootProject.file(value).absolutePath)
      } else {
        project.extensions.extraProperties.set(key, value)
      }
    }
  }
}
