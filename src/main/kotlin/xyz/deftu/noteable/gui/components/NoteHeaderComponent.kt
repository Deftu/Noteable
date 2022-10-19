package xyz.deftu.noteable.gui.components

import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.elementa.state.BasicState
import xyz.deftu.noteable.notes.Note
import xyz.deftu.noteable.notes.NoteManager
import xyz.deftu.noteable.gui.NoteablePalette
import java.awt.Color

class NoteHeaderComponent(
    val note: Note
) : UIContainer() {
    companion object {
        const val HEIGHT = 25
    }

    private val dragState = BasicState(false)
    private var draggingOffset = 0f to 0f

    private val background by UIBlock(NoteablePalette.background2).constrain {
        width = 100.percent
        height = 100.percent
    } childOf this
    private val uuidContainer by UIContainer().constrain {
        x = 5.pixels
        y = CenterConstraint()
        width = 50.percent
        height = 100.percent
    } effect ScissorEffect() childOf background
    private val uuid by UIText(
        text = note.uuid.toString()
    ).constrain {
        y = CenterConstraint()
        textScale = 1.25.pixels
    } childOf uuidContainer

    private val removeButtonContainer by UIContainer().constrain {
        x = 5.pixels(alignOpposite = true)
        y = CenterConstraint()
        width = HEIGHT.pixels
        height = HEIGHT.pixels
    } childOf background
    private val removeButton by UIText("x").constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    } childOf removeButtonContainer

    private val pinButtonContainer by UIContainer().constrain {
        x = SiblingConstraint(alignOpposite = true, padding = 5f)
        y = CenterConstraint()
        width = HEIGHT.pixels
        height = HEIGHT.pixels
    } childOf background
    private val pinButton by UIImage.ofResourceCached("/assets/noteable/pin.png").constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = removeButton.getWidth().pixels + 3.pixels
        height = removeButton.getHeight().pixels
    } childOf pinButtonContainer

    init {
        constrain {
            width = 200.pixels
            height = HEIGHT.pixels
        }

        removeButtonContainer.onMouseEnter {
            removeButton.animate {
                setColorAnimation(Animations.OUT_EXP, 0.75f, NoteablePalette.primary.toConstraint())
            }
        }.onMouseLeave {
            removeButton.animate {
                setColorAnimation(Animations.OUT_EXP, 0.75f, Color.WHITE.toConstraint())
            }
        }.onMouseClick {
            parent.parent.parent.hide(true)
            NoteManager.removeNote(note)
        }

        pinButtonContainer.onMouseEnter {
            pinButton.animate {
                setColorAnimation(Animations.OUT_EXP, 0.75f, NoteablePalette.primary.toConstraint())
            }
        }.onMouseLeave {
            pinButton.animate {
                setColorAnimation(Animations.OUT_EXP, 0.75f, Color.WHITE.toConstraint())
            }
        }.onMouseClick {
            note.sticky = !note.sticky
            NoteManager.removeNote(note)
            NoteManager.addNote(note)
        }

        onMouseClick {
            draggingOffset = it.relativeX to it.relativeY
            dragState.set(true)
        }.onMouseRelease {
            draggingOffset = 0f to 0f
            dragState.set(false)
        }.onMouseDrag { mouseX, mouseY, mouseButton ->
            if (mouseButton != 0 || !dragState.get())
                return@onMouseDrag

            val window = Window.of(this)
            val x = ((mouseX + getLeft() - draggingOffset.first).coerceIn(window.getLeft()..(window.getRight() - parent.getWidth())) / window.getWidth()) * 100
            val y = ((mouseY + getTop()) - draggingOffset.second).coerceIn(window.getTop()..(window.getBottom() - parent.getHeight())) / window.getHeight() * 100
            parent.setX(x.percent)
            parent.setY(y.percent)
            /*if (getTop() >= window.getHeight() / 2) {
                println("y bef: $y")
                y -= getHeight()
                y = y.coerceIn(window.getTop()..(window.getBottom() - parent.getHeight()))
                println("y aft: $y")
            }*/

            note.x = x.toInt()
            note.y = y.toInt()
        }
    }
}
