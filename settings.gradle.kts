import groovy.lang.MissingPropertyException

pluginManagement {
    repositories {
        // Repositories
        maven("https://maven.enhancedpixel.xyz/releases")
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://server.bbkr.space/artifactory/libs-release/")
        maven("https://jitpack.io/")

        // Default repositories
        gradlePluginPortal()
        mavenCentral()

        // Snapshots
        maven("https://maven.enhancedpixel.xyz/snapshots")
        maven("https://s01.oss.sonatype.org/content/groups/public/")
        mavenLocal()
    }

    plugins {
        val kotlin = "1.6.21"
        kotlin("jvm") version(kotlin)
        kotlin("plugin.serialization") version(kotlin)

        val epgt = "1.12.4"
        id("xyz.enhancedpixel.gradle.multiversion-root") version(epgt)
    }
}

val projectName: String = extra["mod.name"]?.toString() ?: throw MissingPropertyException("mod.name has not been set.")
rootProject.name = projectName
rootProject.buildFileName = "build.gradle.kts"

listOf(
    "1.18.2-fabric",
    "1.19.2-fabric"
).forEach { version ->
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../version.gradle.kts"
    }
}