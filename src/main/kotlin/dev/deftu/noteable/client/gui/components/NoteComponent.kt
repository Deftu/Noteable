package dev.deftu.noteable.client.gui.components

import dev.deftu.noteable.note.Note
import dev.deftu.noteable.client.gui.NoteablePalette
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*

class NoteComponent(
    val note: Note
) : UIContainer() {
    init {
        constrain {
            width = 200.pixels
            height = ChildBasedSizeConstraint()
        }
    }

    private val background by UIBlock(NoteablePalette.background).constrain {
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
        color = NoteablePalette.text.toConstraint()
        textScale = 1.25.pixels
    } childOf header
    private val content by UIWrappedText(
        text = note.content
    ).constrain {
        x = 7.5.pixels
        y = SiblingConstraint(padding = 2.5f)
        width = 100.percent - 15.pixels
        color = NoteablePalette.textFaded.toConstraint()
        textScale = 0.8.pixels
    } childOf background
}
