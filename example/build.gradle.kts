plugins { application }

dependencies {
    implementation(project(":lib"))
    implementation(libs.logback.core)
}

application {
    mainClass.set("com.sonianurag.fiber.example.MainKt")
    mainModule.set("com.sonianurag.fiber.example")
}

tasks.compileJava {
    options.compilerArgumentProviders.add(
        CommandLineArgumentProvider {
            listOf(
                "--patch-module",
                "com.sonianurag.fiber.example=${sourceSets["main"].output.asPath}"
            )
        }
    )
}
