package dev.deftu.noteable.client

//#if FABRIC && MC >= 1.16.5
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.deftu.noteable.client.gui.menu.NoteEditorMenu

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory {
        NoteEditorMenu()
    }
}
//#endif
