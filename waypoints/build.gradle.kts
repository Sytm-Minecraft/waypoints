import de.md5lukas.resourceindex.ResourceIndexTask

plugins {
  with(libs.plugins) {
    alias(kotlin)
    alias(shadow)
    alias(minotaur)
    alias(runPaper)
    alias(changelog)
  }
}

description = "Waypoints plugin"

repositories {
  maven("https://repo.codemc.io/repository/maven-public/") // AnvilGUI
  maven("https://libraries.minecraft.net") // Brigadier

  maven("https://repo.mikeprimm.com/") // DynMap
  exclusiveContent { // Pl3xMap
    forRepository { maven("https://api.modrinth.com/maven") }
    filter { includeGroup("maven.modrinth") }
  }
}

dependencies {
  implementation(libs.paper)
  implementation(libs.stdlib)
  implementation(libs.coroutines)

  compileOnly(libs.annotations)

  implementation(project(":utils"))
  implementation(project(":pointers", "shadow"))
  implementation(project(":waypoints-api"))
  implementation(project(":api-base"))
  implementation(project(":api-sqlite", "shadow"))
  implementation(project(":signgui"))

  // Dependencies on own projects
  implementation(libs.md5Commons)
  implementation(libs.kinvs)
  implementation(libs.konfig)

  // Required dependencies
  implementation(libs.schedulers)
  implementation(libs.skedule)
  implementation(libs.anvilGui)
  implementation(libs.bStats)
  implementation(libs.bundles.commandApi)

  // Optional dependencies
  implementation(libs.vaultApi)

  implementation(variantOf(libs.dynmap.coreApi) { classifier("all") })
  implementation(variantOf(libs.dynmap.api) { classifier("unshaded") }) { isTransitive = false }
  implementation(libs.squaremapApi)
  implementation(libs.bluemapApi)
  implementation(libs.pl3xmap)

  // Testing
  testImplementation(kotlin("test-junit5"))
  testImplementation(libs.junitJupiter)
  testRuntimeOnly(libs.junitLauncher)
}

tasks {
  register<ResourceIndexTask>("createResourceIndex")

  processResources {
    dependsOn("createResourceIndex")

    val properties =
        mapOf(
            "version" to project.version,
            "apiVersion" to
                libs.versions.paper.get().substringBefore('-').split('.').take(2).joinToString("."),
            "kotlinVersion" to libs.versions.kotlin.get(),
            "coroutinesVersion" to libs.versions.coroutines.get(),
        )

    inputs.properties(properties)

    filteringCharset = "UTF-8"

    filesMatching("plugin.yml") { expand(properties) }
  }

  compileKotlin {
    // To make sure we have an explicit dependency on the project itself because otherwise we will
    // get
    // a warning that we only depend on an output file and not the project itself
    dependsOn(project(":api-sqlite").tasks["shadowJar"])
    dependsOn(project(":pointers").tasks["shadowJar"])
  }

  shadowJar {
    archiveClassifier.set("")

    minimize {
      exclude(project(":waypoints-api"))
      exclude(project(":utils"))
    }

    exclude("META-INF/")

    dependencies {
      include(project(":utils"))
      include(project(":pointers"))
      include(project(":waypoints-api"))
      include(project(":api-base"))
      include(project(":api-sqlite"))
      include(project(":signgui"))

      include(dependency(libs.md5Commons.get()))
      include(dependency(libs.kinvs.get()))
      include(dependency(libs.konfig.get()))

      include(dependency(libs.schedulers.get()))
      include(dependency(libs.skedule.get()))
      include(dependency(libs.anvilGui.get()))
      include(dependency("org.bstats::"))
    }

    relocate("de.md5lukas.paper.loader", "de.md5lukas.waypoints")
    arrayOf("commons", "kinvs", "konfig", "schedulers", "signgui").forEach {
      relocate("de.md5lukas.$it", "de.md5lukas.waypoints.libs.$it")
    }

    relocate("com.okkero.skedule", "de.md5lukas.waypoints.libs.skedule")
    relocate("net.wesjd.anvilgui", "de.md5lukas.waypoints.libs.anvilgui")
    relocate("org.bstats", "de.md5lukas.waypoints.libs.bstats")
    relocate("io.papermc.papertrail", "de.md5lukas.waypoints.legacy")
  }

  runServer {
    dependsOn("jar")
    minecraftVersion(libs.versions.paper.get().substringBefore('-'))

    downloadPlugins { modrinth("commandapi", libs.versions.commandApi.get()) }
  }

  test { useJUnitPlatform() }
}

runPaper.folia.registerTask()

kotlin { jvmToolchain(libs.versions.jvmToolchain.get().toInt()) }

changelog { path.set(rootProject.relativePath("CHANGELOG.md")) }

modrinth {
  val modrinthToken: String? by project

  token.set(modrinthToken)

  projectId.set("waypoints")
  versionType.set("release")
  uploadFile.set(tasks.shadowJar)

  gameVersions.addAll("1.20.2", libs.versions.paper.get().substringBefore('-'))
  loaders.addAll("paper", "folia")

  syncBodyFrom.set(rootProject.file("README.md").readText())

  changelog.set(
      provider {
        with(project.changelog) {
          renderItem(getLatest().withEmptySections(false).withHeader(false))
        }
      })

  dependencies {
    with(required) { project("commandapi") }
    with(optional) {
      project("pl3xmap")
      project("bluemap")
      project("squaremap")
      project("dynmap")
    }
  }

  debugMode.set(false)
}
