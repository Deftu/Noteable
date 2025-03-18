package dev.deftu.noteable.client.gui.menu

import dev.deftu.noteable.client.gui.components.NoteComponent
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.state.State
import dev.deftu.noteable.client.gui.components.NoteEditModalComponent

class NoteEditHandler(
    window: Window,
    component: NoteComponent
) {
    private lateinit var editState: State<Boolean>
    private lateinit var reloader: () -> Unit

    init {
        component.onMouseClick {
            if (!editState.get())
                return@onMouseClick

            val modal = NoteEditModalComponent(component.note) childOf window
            modal.setReloader(reloader)
        }
    }

    fun setEditState(state: State<Boolean>) {
        editState = state
    }

    fun setReloader(block: () -> Unit) {
        reloader = block
    }
}
