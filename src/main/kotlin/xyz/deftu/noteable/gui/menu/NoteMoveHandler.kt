package xyz.deftu.noteable.gui.menu

import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.State
import xyz.deftu.noteable.gui.components.NoteComponent

class NoteMoveHandler(
    component: NoteComponent
) {
    private lateinit var moveState: State<Boolean>
    private val dragState = BasicState(false)
    private var draggingOffset = 0f to 0f

    init {
        component.onMouseClick {
            if (!moveState.get())
                return@onMouseClick

            draggingOffset = it.relativeX to it.relativeY
            dragState.set(true)
        }.onMouseRelease {
            if (!moveState.get())
                return@onMouseRelease

            draggingOffset = 0f to 0f
            dragState.set(false)
        }.onMouseDrag { mouseX, mouseY, mouseButton ->
            if (mouseButton != 0 || !moveState.get()|| !dragState.get())
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

    fun setMoveState(state: State<Boolean>) {
        moveState = state
    }
}
