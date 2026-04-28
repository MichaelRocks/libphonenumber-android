plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.nmcp.aggregation)
  id("io.michaelrocks.publish-properties")
}

group = "io.michaelrocks"
version = "9.0.29"

tasks.register<Delete>("clean") {
  delete(rootProject.layout.buildDirectory)
}

tasks.register("integrationTest") {
  group = LifecycleBasePlugin.VERIFICATION_GROUP
  description = "Runs sample instrumentation tests on a Gradle-managed local Android device."
  dependsOn(":sample:pixel2Api29DebugAndroidTest")
}

nmcpAggregation {
  centralPortal {
    val centralPortalUsername = (findProperty("centralPortalUsername") as String?)
      ?: System.getenv("CENTRAL_PORTAL_USERNAME")
    val centralPortalPassword = (findProperty("centralPortalPassword") as String?)
      ?: System.getenv("CENTRAL_PORTAL_PASSWORD")

    if (centralPortalUsername != null && centralPortalPassword != null) {
      username = centralPortalUsername
      password = centralPortalPassword
    }

    publishingType = "AUTOMATIC"
  }
}

dependencies {
  // Explicitly list modules to publish to avoid breaking project isolation
  nmcpAggregation(project(":library"))
}
