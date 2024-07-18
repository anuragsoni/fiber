plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.versions)
  alias(libs.plugins.version.catalog.update)
  alias(libs.plugins.dokka)
  alias(libs.plugins.spotless)
}

val ktfmtVersion = "0.51"

repositories { mavenCentral() }

kotlin { jvmToolchain(21) }

spotless {
  kotlin { ktfmt(ktfmtVersion).googleStyle() }

  kotlinGradle { ktfmt(ktfmtVersion).googleStyle() }
}

subprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "org.jetbrains.dokka")
  apply(plugin = "com.diffplug.spotless")

  spotless {
    kotlin { ktfmt(ktfmtVersion).googleStyle() }

    kotlinGradle { ktfmt(ktfmtVersion).googleStyle() }
  }

  repositories { mavenCentral() }

  tasks.withType<Test> { useJUnitPlatform() }

  kotlin { jvmToolchain(21) }
}
