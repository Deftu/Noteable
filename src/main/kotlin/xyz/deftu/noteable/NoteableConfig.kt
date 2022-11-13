package xyz.deftu.noteable

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.isxander.yacl.api.ButtonOption
import dev.isxander.yacl.api.ConfigCategory
import dev.isxander.yacl.api.Option
import dev.isxander.yacl.api.YetAnotherConfigLib
import dev.isxander.yacl.gui.controllers.ActionController
import dev.isxander.yacl.gui.controllers.TickBoxController
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import xyz.deftu.lib.DeftuLib
import xyz.deftu.lib.utils.TextHelper
import xyz.deftu.noteable.gui.NoteEditorMenu
import java.io.File
import java.nio.file.Files

object NoteableConfig {
    @JvmStatic
    val configDir by lazy {
        val configDir = File(FabricLoader.getInstance().configDir.toFile(), "Deftu")
        if (!configDir.exists() && !configDir.mkdirs())
            throw IllegalStateException("Could not create config directory!")

        val modDir = File(configDir, Noteable.ID)
        if (!modDir.exists() && !modDir.mkdirs())
            throw IllegalStateException("Could not create Noteable config directory!")

        modDir
    }

    private val configFile by lazy {
        File(configDir, "config.json").toPath()
    }

    var gettingStarted = false
    var enabled = true
    var updateChecking = true

    @JvmStatic
    fun save() {
        Files.deleteIfExists(configFile)

        val json = JsonObject()
        json.addProperty("getting_started", gettingStarted)
        json.addProperty("enabled", enabled)
        json.addProperty("update_checking", updateChecking)

        Files.writeString(configFile, json.toString())
    }

    @JvmStatic
    fun load() {
        if (Files.notExists(configFile)) {
            save()
            return
        }

        val element = Files.readString(configFile).let(JsonParser::parseString)
        if (!element.isJsonObject) {
            save()
            return
        }

        val json = element.asJsonObject
        gettingStarted = json.get("getting_started")?.asBoolean ?: save().let { return }
        enabled = json.get("enabled")?.asBoolean ?: save().let { return }
        updateChecking = json.get("update_checking")?.asBoolean ?: save().let { return }
    }

    @JvmStatic
    fun createMenu(parent: Screen? = MinecraftClient.getInstance().currentScreen) = YetAnotherConfigLib.createBuilder()
        .title(TextHelper.createTranslatableText(Noteable.NAME))
        .category(ConfigCategory.createBuilder()
            .name(TextHelper.createLiteralText(Noteable.NAME))
            .option(Option.createBuilder(Boolean::class.java)
                .name(TextHelper.createTranslatableText("${Noteable.ID}.config.enabled"))
                .binding(true, ::enabled) { value ->
                    enabled = value
                }.controller { option ->
                    TickBoxController(option)
                }.build())
            .option(Option.createBuilder(Boolean::class.java)
                .name(TextHelper.createTranslatableText("${Noteable.ID}.config.update_checking"))
                .binding(true, ::updateChecking) { value ->
                    updateChecking = value
                }.controller { option ->
                    TickBoxController(option)
                }.build())
            .option(ButtonOption.createBuilder()
                .name(TextHelper.createTranslatableText("${Noteable.ID}.config.edit"))
                .action { screen, option ->
                    MinecraftClient.getInstance().setScreen(NoteEditorMenu(Noteable.hudWindow))
                }.controller { option ->
                    ActionController(option)
                }.build())
            .build())
        .save(::save)
        .build()
        .generateScreen(parent)
}
