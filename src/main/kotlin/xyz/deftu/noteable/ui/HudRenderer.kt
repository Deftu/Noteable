package xyz.deftu.noteable.ui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.UMatrixStack
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import xyz.deftu.noteable.events.NoteActionEvent
import xyz.deftu.noteable.ui.components.NoteComponent

object HudRenderer {
    private val window = Window(ElementaVersion.V2)

    fun initialize() {
        HudRenderCallback.EVENT.register { stack, _ ->
            window.draw(UMatrixStack(stack))
        }

        NoteActionEvent.EVENT.register { action, note ->
            println("note action: ($action - $note)")
            if (!note.sticky) return@register
            when (action) {
                NoteActionEvent.NoteAction.CREATE -> NoteComponent(note).constrain {
                    x = note.x.percent
                    y = note.y.percent
                } effect OutlineEffect(NoteablePalette.primary, 1f) childOf window
                NoteActionEvent.NoteAction.REMOVE -> {
                    window.childrenOfType<NoteComponent>().filter {
                        it.note.uuid == note.uuid
                    }.forEach(window::removeChild)
                }
            }
        }
    }
}
