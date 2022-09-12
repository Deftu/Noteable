import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.modrinth.minotaur.dependencies.ModDependency
import com.modrinth.minotaur.dependencies.DependencyType
import xyz.unifycraft.gradle.utils.GameSide
import xyz.unifycraft.gradle.tools.CurseDependency

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("xyz.unifycraft.gradle.multiversion")
    id("xyz.unifycraft.gradle.tools")
    id("xyz.unifycraft.gradle.tools.loom")
}

loomHelper {
    disableRunConfigs(GameSide.SERVER)
}

repositories {
    maven("https://maven.terraformersmc.com/")
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

    modImplementation("net.fabricmc.fabric-api:fabric-api:${when (mcData.version) {
        11902 -> "0.60.0+1.19.2"
        11802 -> "0.58.0+1.18.2"
        else -> throw IllegalStateException("Invalid MC version: ${mcData.version}")
    }}")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.8.2+kotlin.1.7.10")

    modImplementation("com.terraformersmc:modmenu:${when (mcData.version) {
        11902 -> "4.0.6"
        11802 -> "3.2.3"
        else -> throw IllegalStateException("Invalid MC version: ${mcData.version}")
    }}")

    include(modImplementation(libs.versions.universalcraft.map {
        "gg.essential:universalcraft-${when (mcData.version) {
            11902 -> "1.19-fabric"
            11802 -> "1.18.1-fabric"
            else -> "${mcData.versionStr}-${mcData.loader.name}"
        }}:$it"
    }.get()).excludeVitals())
    include(modImplementation(libs.versions.elementa.map {
        "gg.essential:elementa-${when (mcData.version) {
            11902 -> "1.18.1-fabric"
            11802 -> "1.18.1-fabric"
            else -> "${mcData.versionStr}-${mcData.loader.name}"
        }}:$it"
    }.get()).excludeVitals())
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
