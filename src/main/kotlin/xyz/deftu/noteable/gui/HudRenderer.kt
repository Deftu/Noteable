package xyz.deftu.noteable.gui

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import xyz.deftu.lib.client.DeftuLibClient
import xyz.deftu.noteable.events.ConfigUpdateEvent
import xyz.deftu.noteable.events.NoteActionEvent
import xyz.deftu.noteable.events.NoteConfigureEvent
import xyz.deftu.noteable.notes.Note
import xyz.deftu.noteable.gui.components.NoteComponent
import xyz.deftu.noteable.notes.NoteManager

object HudRenderer {
    private val noteContainer by UIContainer().constrain {
        width = 100.percent
        height = 100.percent
    }

    fun initialize() {
        DeftuLibClient.hudWindow.window.addChild(noteContainer)

        ConfigUpdateEvent.EVENT.register { config ->
            noteContainer.clearChildren()
            if (config.enabled) {
                configureNotes(NoteManager.getNotes())
            }
        }

        NoteActionEvent.EVENT.register { action, note ->
            if (!note.sticky) return@register
            when (action) {
                NoteActionEvent.NoteAction.CREATE -> note.createComponent() childOf noteContainer
                NoteActionEvent.NoteAction.REMOVE -> {
                    noteContainer.childrenOfType<NoteComponent>().filter {
                        it.note.uuid == note.uuid
                    }.forEach(noteContainer::removeChild)
                }
            }
        }

        NoteConfigureEvent.EVENT.register { notes ->
            noteContainer.clearChildren()
            configureNotes(notes)
        }
    }

    private fun configureNotes(notes: List<Note>) {
        notes.forEach { note ->
            if (!note.sticky)
                return@forEach

            note.createComponent() childOf noteContainer
        }
    }

    private fun Note.createComponent() = NoteComponent(this).constrain {
        x = this@createComponent.x.percent
        y = this@createComponent.y.percent
    } effect OutlineEffect(NoteablePalette.primary, 1f)
}
