plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.versions)
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.dokka)
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")

    repositories { mavenCentral() }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    kotlin {
        jvmToolchain(21)
    }
}