package xyz.deftu.noteable.gui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.UMatrixStack
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import xyz.deftu.noteable.events.NoteActionEvent
import xyz.deftu.noteable.events.NoteConfigureEvent
import xyz.deftu.noteable.notes.Note
import xyz.deftu.noteable.gui.components.NoteComponent

object HudRenderer {
    private val window = Window(ElementaVersion.V2)

    fun initialize() {
        HudRenderCallback.EVENT.register { stack, _ ->
            window.draw(UMatrixStack(stack))
        }

        NoteActionEvent.EVENT.register { action, note ->
            if (!note.sticky) return@register
            when (action) {
                NoteActionEvent.NoteAction.CREATE -> note.createComponent() childOf window
                NoteActionEvent.NoteAction.REMOVE -> {
                    window.childrenOfType<NoteComponent>().filter {
                        it.note.uuid == note.uuid
                    }.forEach(window::removeChild)
                }
            }
        }

        NoteConfigureEvent.EVENT.register { notes ->
            window.clearChildren()
            notes.forEach { note ->
                if (!note.sticky)
                    return@forEach

                note.createComponent() childOf window
            }
        }
    }

    private fun Note.createComponent() = NoteComponent(this).constrain {
        x = this@createComponent.x.percent
        y = this@createComponent.y.percent
    } effect OutlineEffect(NoteablePalette.primary, 1f)
}
