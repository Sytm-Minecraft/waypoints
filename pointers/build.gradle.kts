import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("jvm")
    alias(libs.plugins.dokka)
    `maven-publish`
}

repositories {
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    api(libs.paper)
    api(kotlin("stdlib"))

    implementation(libs.schedulers)
    implementation(libs.protocollib)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val dokkaHtml by tasks.getting(DokkaTask::class) {
    dokkaSourceSets {
        configureEach {
            val majorVersion = libs.versions.paper.get().split('.').let { "${it[0]}.${it[1]}" }
            externalDocumentationLink("https://jd.papermc.io/paper/$majorVersion/", "https://jd.papermc.io/paper/$majorVersion/element-list")
        }
    }
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn(tasks.dokkaJavadoc)
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc)
}

val dokkaHtmlJar by tasks.creating(Jar::class) {
    dependsOn(tasks.dokkaHtml)
    archiveClassifier.set("dokka")
    from(tasks.dokkaHtml)
}

publishing {
    repositories {
        maven {
            name = "md5lukasReposilite"

            url = uri(
                "https://repo.md5lukas.de/${
                    if (version.toString().endsWith("-SNAPSHOT")) {
                        "snapshots"
                    } else {
                        "releases"
                    }
                }"
            )

            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
            artifact(sourcesJar)
            artifact(javadocJar)
            artifact(dokkaHtmlJar)
        }
    }
}