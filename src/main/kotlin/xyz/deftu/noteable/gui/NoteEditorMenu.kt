package xyz.deftu.noteable.gui

import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.dsl.*
import xyz.deftu.lib.client.gui.context.ContextMenu
import xyz.deftu.lib.client.gui.context.ContextMenuComponent
import xyz.deftu.lib.client.gui.context.ContextMenuItem
import xyz.deftu.lib.client.gui.hud.DraggableHudMenu
import xyz.deftu.lib.client.hud.DraggableHudWindow
import xyz.deftu.lib.client.hud.HudComponent
import xyz.deftu.lib.utils.TextHelper
import xyz.deftu.noteable.Noteable
import xyz.deftu.noteable.notes.NoteManager

class NoteEditorMenu(
    hudWindow: DraggableHudWindow
) : DraggableHudMenu(hudWindow) {
    companion object {
        var instance: NoteEditorMenu? = null
            private set
    }

    init {
        instance = this
        Inspector(window) childOf window

        window.onMouseClick {
            if (it.mouseButton == 1 && it.target == container) {
                it.stopImmediatePropagation()
                val contextMenu by ContextMenu.create(
                    xPos = it.absoluteX,
                    yPos = it.absoluteY,
                    item = arrayOf(
                        ContextMenu.item(TextHelper.createTranslatableText("${Noteable.ID}.menu.add")) { item ->
                            NoteCreateModalComponent() childOf window
                            item.closeParent()
                        }
                    )
                ).setBaseComponent(container) childOf window
            }
        }
    }

    override fun getDefaultContextMenuItems(component: HudComponent): List<ContextMenuItem> {
        if (component !is NoteComponent) throw IllegalArgumentException("Component must be a NoteComponent")

        return listOf(
            ContextMenu.item(TextHelper.createTranslatableText("${Noteable.ID}.menu.edit")) { item ->
                NoteEditModalComponent(component.note) childOf window
                item.closeParent()
            },
            ContextMenu.item(TextHelper.createTranslatableText("${Noteable.ID}.menu.pin")) { item ->
                component.note.sticky = !component.note.sticky
                component.configurePinComponent()
                item.closeParent()
            }
        )
    }

    override fun setupHudComponent(state: State, component: HudComponent) {
        if (state != State.POST) return
        if (component !is NoteComponent) throw IllegalArgumentException("Component must be a NoteComponent")

        component.configurePinComponent()
    }

    override fun onScreenClose() {
        NoteManager.save()
        super.onScreenClose()
    }

    private fun NoteComponent.configurePinComponent() {
        if (note.sticky) {
            title.constrain {
                width = 100.percent - 25.pixels
            }
            pinComponent.unhide(true)
        } else {
            pinComponent.hide(true)
            title.constrain {
                width = 100.percent - 15.pixels
            }
        }
    }
}
