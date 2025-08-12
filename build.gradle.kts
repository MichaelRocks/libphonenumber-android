plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.nmcp.aggregation)
  id("io.michaelrocks.publish-properties")
}

group = "io.michaelrocks"
version = "9.0.5"

tasks.register<Delete>("clean") {
  delete(rootProject.layout.buildDirectory)
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

    publishingType = "USER_MANAGED"
  }
}

dependencies {
  // Explicitly list modules to publish to avoid breaking project isolation
  nmcpAggregation(project(":library"))
}
