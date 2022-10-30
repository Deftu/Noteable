import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import xyz.enhancedpixel.gradle.utils.GameSide

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("xyz.enhancedpixel.gradle.multiversion")
    id("xyz.enhancedpixel.gradle.tools")
    id("xyz.enhancedpixel.gradle.tools.loom")
}

loomHelper {
    disableRunConfigs(GameSide.SERVER)
}

repositories {
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.deftu.xyz/releases")
    mavenCentral()
}

fun Dependency?.excludeVitals(): Dependency = apply {
    check(this is ExternalModuleDependency)
    exclude(module = "kotlin-stdlib")
    exclude(module = "kotlin-stdlib-common")
    exclude(module = "kotlin-stdlib-jdk8")
    exclude(module = "kotlin-stdlib-jdk7")
    exclude(module = "kotlin-reflect")
    exclude(module = "annotations")
    exclude(module = "fabric-loader")
}!!

dependencies {
    implementation(kotlin("stdlib"))
    modImplementation("xyz.deftu:DeftuLib-${mcData.versionStr}:1.0.0")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjvm-default=enable"
        }
    }

    remapJar {
        archiveBaseName.set("${modData.name}-${mcData.versionStr}")
    }
}
