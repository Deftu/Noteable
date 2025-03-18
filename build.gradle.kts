import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import dev.deftu.gradle.utils.GameSide
import dev.deftu.gradle.utils.includeOrShade
import dev.deftu.gradle.utils.version.MinecraftVersions
import com.modrinth.minotaur.dependencies.DependencyType
import com.modrinth.minotaur.dependencies.ModDependency

plugins {
    java
    kotlin("jvm")
    id("dev.deftu.gradle.multiversion")
    id("dev.deftu.gradle.tools")
    id("dev.deftu.gradle.tools.resources")
    id("dev.deftu.gradle.tools.bloom")
    id("dev.deftu.gradle.tools.shadow")
    id("dev.deftu.gradle.tools.publishing.maven")
    id("dev.deftu.gradle.tools.minecraft.loom")
    id("dev.deftu.gradle.tools.minecraft.releases")
}

toolkitLoomHelper {
    disableRunConfigs(GameSide.SERVER)
}

dependencies {
    val textileVersion = "0.11.1"
    val omnicoreVersion = "0.23.0"
    implementation("dev.deftu:textile:$textileVersion")
    modImplementation("dev.deftu:textile-$mcData:$textileVersion")
    modImplementation("dev.deftu:omnicore-$mcData:$omnicoreVersion")
    modImplementation(includeOrShade("gg.essential:universalcraft-${when (mcData.version) {
        MinecraftVersions.VERSION_1_16_5 -> "1.16.2"
        MinecraftVersions.VERSION_1_18_2 -> "1.18.1"
        MinecraftVersions.VERSION_1_21_1, MinecraftVersions.VERSION_1_21_2 -> "1.21"
        else -> mcData.version.toString()
    }}-${mcData.loader}:375")!!)
    implementation(includeOrShade("gg.essential:elementa:695") {
        exclude("gg.essential", "universalcraft")
    })

    if (mcData.version <= MinecraftVersions.VERSION_1_12_2) {
        includeOrShade("dev.deftu:textile:$textileVersion")
        includeOrShade("dev.deftu:textile-$mcData:$textileVersion")
        includeOrShade("dev.deftu:omnicore-$mcData:$omnicoreVersion")
    }

    if (mcData.isFabric) {
        if (mcData.isLegacyFabric) {
            // 1.8.9 - 1.13
            modImplementation("net.legacyfabric.legacy-fabric-api:legacy-fabric-api:${mcData.dependencies.legacyFabric.legacyFabricApiVersion}")
        } else {
            // 1.16.5+
            modImplementation("net.fabricmc.fabric-api:fabric-api:${mcData.dependencies.fabric.fabricApiVersion}")
            modImplementation(mcData.dependencies.fabric.modMenuDependency)
        }
    }
}

toolkitReleases {
    val changelog = rootProject.file("changelogs/${project.version}.md")
    if (changelog.exists()) {
        changelogFile.set(changelog)
    }
}
