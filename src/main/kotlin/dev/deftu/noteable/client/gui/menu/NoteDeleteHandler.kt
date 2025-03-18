package dev.deftu.noteable.client.gui.menu

import dev.deftu.noteable.client.ClientNoteManager
import dev.deftu.noteable.client.gui.components.NoteComponent
import gg.essential.elementa.state.State

class NoteDeleteHandler(
    component: NoteComponent
) {

    private lateinit var deleteState: State<Boolean>
    private lateinit var reloader: () -> Unit

    init {
        component.onMouseClick {
            if (!deleteState.get()) {
                return@onMouseClick
            }

            ClientNoteManager.remove(component.note)
            reloader()
        }
    }

    fun setDeleteState(state: State<Boolean>) {
        deleteState = state
    }

    fun setReloader(block: () -> Unit) {
        reloader = block
    }

}
