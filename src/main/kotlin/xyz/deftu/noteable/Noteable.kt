package xyz.deftu.noteable

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import org.lwjgl.glfw.GLFW
import xyz.deftu.lib.client.hud.DraggableHudWindow
import xyz.deftu.lib.events.InputEvent
import xyz.deftu.noteable.events.NoteActionEvent
import xyz.deftu.noteable.gui.NoteEditorMenu
import xyz.deftu.noteable.notes.NoteManager

object Noteable : ClientModInitializer {
    const val NAME = "@MOD_NAME@"
    const val VERSION = "@MOD_VERSION@"
    const val ID = "@MOD_ID@"

    const val NOTES_CONFIG = "notes.json"

    val hudWindow = DraggableHudWindow()

    override fun onInitializeClient() {
        NoteableConfig.load()
        hudWindow.initialize()
        NoteManager.initialize()

        NoteActionEvent.EVENT.register { action, note ->
            println("NoteActionEvent: $action $note - refresh")
            NoteEditorMenu.instance?.refresh()
        }

        val keyBinding = KeyBinding("$ID.key.open_editor", GLFW.GLFW_KEY_N, NAME)
        KeyBindingHelper.registerKeyBinding(keyBinding)
        InputEvent.EVENT.register { button, action, _, type ->
            if (action != GLFW.GLFW_RELEASE || MinecraftClient.getInstance().currentScreen != null || !keyBinding.matchesKey(button, 0))
                return@register

            MinecraftClient.getInstance().setScreen(NoteableConfig.createMenu())
        }
    }
}
