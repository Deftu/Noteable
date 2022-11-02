package xyz.deftu.noteable

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import org.lwjgl.glfw.GLFW
import xyz.deftu.lib.events.InputEvent
import xyz.deftu.noteable.notes.NoteManager
import xyz.deftu.noteable.gui.HudRenderer
import xyz.deftu.noteable.gui.menu.NoteEditorMenu
import java.io.File

object Noteable : ClientModInitializer {
    const val NAME = "@MOD_NAME@"
    const val VERSION = "@MOD_VERSION@"
    const val ID = "@MOD_ID@"

    const val NOTES_CONFIG = "notes.json"

    override fun onInitializeClient() {
        HudRenderer.initialize()
        NoteManager.initialize()

        val keyBinding = KeyBinding("key.$ID.open_editor", GLFW.GLFW_KEY_N, "key.categories.noteable")
        KeyBindingHelper.registerKeyBinding(keyBinding)
        InputEvent.EVENT.register { button, action, _, type ->
            if (action != GLFW.GLFW_RELEASE || MinecraftClient.getInstance().currentScreen != null || !keyBinding.matchesKey(button, 0))
                return@register

            MinecraftClient.getInstance().setScreen(NoteEditorMenu())
        }
    }

    fun getConfigDirectory(): File {
        val dir = File("config")
        if (!dir.exists() && !dir.mkdirs())
            throw IllegalStateException("Could not create config directory!")

        val noteableDir = File(dir, ID)
        if (!noteableDir.exists() && !noteableDir.mkdirs())
            throw IllegalStateException("Could not create Noteable config directory!")

        return noteableDir
    }
}
