plugins { application }

application.mainClass.set("MainKt")

dependencies {
  implementation(libs.kotlinx.coroutines)
  implementation(project(":fiber"))
  implementation(libs.logback.core)
}
