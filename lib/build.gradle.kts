plugins {
    `java-library`
    alias(libs.plugins.kover)
}

dependencies {
    api(libs.kotlinx.coroutines)
    implementation(libs.netty.all)
    implementation(libs.slf4j.api)
    testImplementation(libs.jqwik.kotlin)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.kotlin.junit5)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.launcher)
}
