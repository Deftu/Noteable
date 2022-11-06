package xyz.deftu.noteable.gui.menu

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.State
import gg.essential.universal.UKeyboard
import xyz.deftu.noteable.gui.components.NoteComponent

class NoteMoveHandler(
    private val component: NoteComponent
) {
    private lateinit var moveState: State<Boolean>
    private val dragState = BasicState(false)
    private var draggingOffset = 0f to 0f

    init {
        component.onMouseClick {
            if (!moveState.get())
                return@onMouseClick

            grabWindowFocus()
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

            move(parent, mouseX + getLeft(), mouseY + getTop())
        }.onKeyType { typedChar, keyCode ->
            draggingOffset = 0f to 0f
            when (keyCode) {
                UKeyboard.KEY_ESCAPE -> dragState.set(false)
                UKeyboard.KEY_LEFT -> move(parent, getLeft() - 1, getTop())
                UKeyboard.KEY_RIGHT -> move(parent, getLeft() + 1, getTop())
                UKeyboard.KEY_UP -> move(parent, getLeft(), getTop() - 1)
                UKeyboard.KEY_DOWN -> move(parent, getLeft(), getTop() + 1)
            }
        }
    }

    private fun move(parent: UIComponent, offsetX: Float, offsetY: Float) {
        val note = component.note
        val window = Window.of(component)
        val x = (offsetX - draggingOffset.first).coerceIn(window.getLeft()..(window.getRight() - component.getWidth())) / window.getWidth() * 100
        val y = (offsetY - draggingOffset.second).coerceIn(window.getTop()..(window.getBottom() - component.getHeight())) / window.getHeight() * 100
        parent.setX(x.percent)
        parent.setY(y.percent)
        note.x = x.toInt()
        note.y = y.toInt()
    }

    fun setMoveState(state: State<Boolean>) {
        moveState = state
    }
}
