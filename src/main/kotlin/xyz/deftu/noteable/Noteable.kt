package xyz.deftu.noteable

import net.fabricmc.api.ClientModInitializer
import xyz.deftu.noteable.notes.NoteManager
import xyz.deftu.noteable.ui.HudRenderer

object Noteable : ClientModInitializer {
    const val NOTES_CONFIG = "notes.json"

    override fun onInitializeClient() {
        HudRenderer.initialize()
        NoteManager.initialize()
    }
}
