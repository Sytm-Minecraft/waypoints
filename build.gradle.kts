import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlin) apply false
  alias(libs.plugins.spotless)
}

repositories { mavenCentral() }

subprojects {
  repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.md5lukas.de/public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://jitpack.io") // Vault
  }

  tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs.addAll(
        "-Xjvm-default=all",
        "-Xlambdas=indy",
    )
  }
}

spotless {
  java {
    target(
        "**/src/*/java/**/*.java",
    )
    palantirJavaFormat().style("GOOGLE")
    formatAnnotations()
  }
  kotlin {
    toggleOffOn()
    target(
        "*/src/*/kotlin/**/*.kt",
        "**/*.kts",
    )
    ktfmt()
  }
}
