package xyz.deftu.noteable.ui.components

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.BasicState
import xyz.deftu.noteable.notes.Note
import xyz.deftu.noteable.ui.NoteablePalette

class NoteHeaderComponent(
    val note: Note
) : UIContainer() {
    companion object {
        const val HEIGHT = 25
    }

    private val dragState = BasicState(false)
    private var draggingOffset = 0f

    private val background by UIBlock(NoteablePalette.background).constrain {
        width = 100.percent
        height = 100.percent
    } childOf this
    private val uuid by UIWrappedText(
        text = note.uuid.toString(),
        trimText = true
    ).constrain {
        x = 5.pixels
        y = CenterConstraint()
        width = 50.percent
        textScale = 1.25.pixels
    } childOf background

    init {
        constrain {
            width = 200.pixels
            height = HEIGHT.pixels
        }

        onMouseClick {
            draggingOffset = it.relativeX - (getWidth() / 2)
            println("relative x: ${it.relativeX}")
            println("dragging off: $draggingOffset")
            dragState.set(true)
        }.onMouseRelease {
            dragState.set(false)
        }.onMouseDrag { mouseX, mouseY, mouseButton ->
            if (!dragState.get()) return@onMouseDrag

            val window = Window.of(this)
            val x = ((mouseX + getLeft()).coerceIn(window.getLeft()..(window.getRight() - parent.getWidth())) / window.getWidth()) * 100
            val y = ((mouseY + getTop()).coerceIn(window.getTop()..(window.getBottom() - parent.getHeight())) / window.getHeight()) * 100
            println("x: $x")
            println("y: $y")
            parent.setX(x.percent)
            parent.setY(y.percent)
            note.x = x.toInt()
            note.y = y.toInt()
        }
    }
}
