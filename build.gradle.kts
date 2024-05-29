plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.versions)
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
}

repositories { mavenCentral() }

kotlin { jvmToolchain(21) }

spotless {
    kotlin { ktfmt().kotlinlangStyle() }

    kotlinGradle { ktfmt().kotlinlangStyle() }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.diffplug.spotless")

    spotless {
        kotlin { ktfmt().kotlinlangStyle() }

        kotlinGradle { ktfmt().kotlinlangStyle() }
    }

    repositories { mavenCentral() }

    tasks.withType<Test> { useJUnitPlatform() }

    kotlin { jvmToolchain(21) }
}
