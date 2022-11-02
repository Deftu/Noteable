import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import xyz.enhancedpixel.gradle.utils.GameSide
import xyz.enhancedpixel.gradle.tools.CurseRelation
import xyz.enhancedpixel.gradle.tools.CurseRelationType
import com.modrinth.minotaur.dependencies.DependencyType
import com.modrinth.minotaur.dependencies.ModDependency

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("xyz.enhancedpixel.gradle.multiversion")
    id("xyz.enhancedpixel.gradle.tools")
    id("xyz.enhancedpixel.gradle.tools.loom")
    id("xyz.enhancedpixel.gradle.tools.blossom")
    id("xyz.enhancedpixel.gradle.tools.releases")
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
    modImplementation(include("xyz.deftu:DeftuLib-${mcData.versionStr}:1.2.0")!!)
}

releases {
    modrinth {
        projectId.set("8CZovXCd")
        dependencies.set(listOf(
            ModDependency("P7dR8mSH", DependencyType.REQUIRED),
            ModDependency("Ha28R6CL", DependencyType.REQUIRED),
            ModDependency("mOgUt4GM", DependencyType.REQUIRED),
            ModDependency("WfhjX9sQ", DependencyType.REQUIRED)
        ))
    }

    curseforge {
        projectId.set("690080")
        relations.set(listOf(
            CurseRelation("fabric-api", CurseRelationType.REQUIRED),
            CurseRelation("fabric-language-kotlin", CurseRelationType.REQUIRED),
            CurseRelation("modmenu", CurseRelationType.REQUIRED),
            CurseRelation("deftulib", CurseRelationType.REQUIRED)
        ))
    }
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
