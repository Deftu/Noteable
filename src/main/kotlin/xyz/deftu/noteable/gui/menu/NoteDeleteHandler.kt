package xyz.deftu.noteable.gui.menu

import gg.essential.elementa.state.State
import xyz.deftu.noteable.gui.components.NoteComponent
import xyz.deftu.noteable.notes.NoteManager

class NoteDeleteHandler(
    component: NoteComponent
) {
    private lateinit var deleteState: State<Boolean>
    private lateinit var reloader: () -> Unit

    init {
        component.onMouseClick {
            if (!deleteState.get())
                return@onMouseClick

            NoteManager.removeNote(component.note)
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
