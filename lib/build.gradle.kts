plugins {
    `java-library`
    alias(libs.plugins.kover)
}

dependencies {
    api(libs.kotlinx.coroutines)
    implementation(libs.netty.buffer)
    implementation(libs.netty.codec.http)
    implementation(libs.netty.codec.http2)
    implementation(libs.netty.handler)
    implementation(libs.netty.transport.native.epoll) { artifact { classifier = "linux-aarch_64" } }
    implementation(libs.netty.transport.native.epoll) { artifact { classifier = "linux-x86_64" } }
    implementation(libs.netty.transport.native.kqueue) { artifact { classifier = "osx-aarch_64" } }
    implementation(libs.netty.transport.native.kqueue) { artifact { classifier = "osx-x86_64" } }
    implementation(libs.slf4j.api)
    testImplementation(libs.okhttp)
    testImplementation(libs.jqwik.kotlin)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.kotlin.junit5)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.launcher)
}
