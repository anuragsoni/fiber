import org.gradle.nativeplatform.platform.internal.ArchitectureInternal
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
  `java-library`
  alias(libs.plugins.kover)
}

fun getClassifierForArchitecture(arch: ArchitectureInternal): String? {
  return if (arch.isArm64) {
    "aarch_64"
  } else if (arch.isAmd64) {
    "x86_64"
  } else {
    null
  }
}

dependencies {
  implementation(libs.kotlinx.coroutines)
  implementation(libs.netty.transport.native.epoll)
  implementation(libs.netty.transport.native.kqueue)
  implementation(libs.slf4j.api)
  implementation(libs.vertx.core)
  implementation(libs.vertx.kotlin)
  implementation(libs.vertx.kotlin.coroutines)

  val currentOS = DefaultNativePlatform.getCurrentOperatingSystem()
  val currentArchitecture = DefaultNativePlatform.getCurrentArchitecture()

  if (currentOS.isLinux) {
    implementation(libs.netty.transport.native.epoll) {
      artifact { classifier = "linux-${getClassifierForArchitecture(currentArchitecture)}" }
    }
  } else if (currentOS.isMacOsX) {
    implementation(libs.netty.transport.native.kqueue) {
      artifact { classifier = "osx-${getClassifierForArchitecture(currentArchitecture)}" }
    }
  } else {
    logger.debug(
      "Using Nio since no native transport was available on {} and {}.",
      currentOS.displayName,
      currentArchitecture.displayName,
    )
  }

  testImplementation(libs.jqwik.kotlin)
  testImplementation(libs.junit.jupiter.engine)
  testImplementation(libs.kotlin.junit5)
  testImplementation(libs.kotlinx.coroutines.test)
  testRuntimeOnly(libs.junit.launcher)
}
