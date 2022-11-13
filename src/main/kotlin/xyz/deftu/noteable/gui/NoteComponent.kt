package xyz.deftu.noteable.gui

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import xyz.deftu.lib.client.gui.DeftuPalette
import xyz.deftu.lib.client.hud.HudComponent
import xyz.deftu.noteable.notes.Note
import xyz.deftu.noteable.notes.NoteManager

class NoteComponent(
    val note: Note
) : HudComponent() {
    private val background by UIBlock(DeftuPalette.getBackground()).constrain {
        width = 100.percent
        height = 15.pixels + ChildBasedSizeConstraint(padding = 2.5f)
    } childOf this

    val header by UIContainer().constrain {
        width = 100.percent
        height = ChildBasedMaxSizeConstraint() + 7.5.pixels
    } childOf background
    val title by UIWrappedText(
        text = note.title
    ).constrain {
        x = 7.5.pixels
        y = 7.5.pixels
        width = 100.percent - 15.pixels
        color = DeftuPalette.getText().toConstraint()
        textScale = 1.25.pixels
    } childOf header
    var pinComponent by UIImage.ofResourceCached("/assets/noteable/pin.png").constrain {
        x = 7.5.pixels(true)
        y = 7.5.pixels
        width = 8.pixels
        height = 8.pixels
    } childOf header
    private val content by UIWrappedText(
        text = note.content
    ).constrain {
        x = 7.5.pixels
        y = SiblingConstraint(padding = 2.5f)
        width = 100.percent - 15.pixels
        color = DeftuPalette.getTextFaded().toConstraint()
        textScale = 0.8.pixels
    } childOf background

    init {
        constrain {
            width = 200.pixels
            height = ChildBasedSizeConstraint()
        }

        onRemove {
            NoteManager.removeNote(note)
        }

        onMove { posX, posY ->
            note.x = posX
            note.y = posY
            println("note moved to ${posX}, ${posY} ($note)")
        }

        pinComponent.hide(true)
    }
}
