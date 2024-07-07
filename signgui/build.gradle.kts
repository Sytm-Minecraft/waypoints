plugins {
  `java-library`
  `maven-publish`
}

dependencies {
  api(libs.paper)
  compileOnly(libs.annotations)
  implementation(libs.protocollib)
}

java {
  toolchain.languageVersion = JavaLanguageVersion.of(libs.versions.jvmToolchain.get().toInt())
  withSourcesJar()
  withJavadocJar()
}

group = "de.md5lukas"

version = project.property("signGuiVersion") as String

publishing {
  repositories {
    maven {
      name = "md5lukasReposilite"

      url =
          uri(
              "https://repo.md5lukas.de/${
            if (version.toString().endsWith("-SNAPSHOT")) {
              "snapshots"
            } else {
              "releases"
            }
          }")

      credentials(PasswordCredentials::class)
      authentication { create<BasicAuthentication>("basic") }
    }
  }
  publications { create<MavenPublication>("maven") { from(components["java"]) } }
}
