package xyz.deftu.noteable.gui.menu

import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.State
import xyz.deftu.noteable.gui.components.NoteComponent

class NoteDragHandler(
    component: NoteComponent
) {
    private lateinit var editState: State<Boolean>
    private val dragState = BasicState(false)
    private var draggingOffset = 0f to 0f

    init {
        component.onMouseClick {
            if (!editState.get())
                return@onMouseClick

            draggingOffset = it.relativeX to it.relativeY
            dragState.set(true)
        }.onMouseRelease {
            if (!editState.get())
                return@onMouseRelease

            draggingOffset = 0f to 0f
            dragState.set(false)
        }.onMouseDrag { mouseX, mouseY, mouseButton ->
            if (mouseButton != 0 || !editState.get()|| !dragState.get())
                return@onMouseDrag

            val note = component.note
            val window = Window.of(this)
            val x = ((mouseX + getLeft() - draggingOffset.first).coerceIn(window.getLeft()..(window.getRight() - getWidth())) / window.getWidth()) * 100
            val y = ((mouseY + getTop()) - draggingOffset.second).coerceIn(window.getTop()..(window.getBottom() - getHeight())) / window.getHeight() * 100
            parent.setX(x.percent)
            parent.setY(y.percent)
            note.x = x.toInt()
            note.y = y.toInt()
        }
    }

    fun setEditState(state: State<Boolean>) {
        editState = state
    }
}
