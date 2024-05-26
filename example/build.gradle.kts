plugins {
    application
}

dependencies {
    implementation(project(":lib"))
    implementation(libs.logback.core)
}

application.mainClass.set("MainKt")