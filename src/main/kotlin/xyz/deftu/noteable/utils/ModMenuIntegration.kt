package xyz.deftu.noteable.utils

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import xyz.deftu.noteable.gui.menu.NoteEditorMenu

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory {
        NoteEditorMenu()
    }
}
