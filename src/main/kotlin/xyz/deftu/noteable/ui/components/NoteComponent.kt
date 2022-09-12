package xyz.deftu.noteable.ui.components

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import xyz.deftu.noteable.notes.Note
import xyz.deftu.noteable.ui.NoteablePalette

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

    private val title by UIWrappedText(
        text = note.title
    ).constrain {
        x = 7.5.pixels
        y = 7.5.pixels
        width = 100.percent - 7.5.pixels
        color = NoteablePalette.text.toConstraint()
        textScale = 1.25.pixels
    } childOf background
    private val content by UIWrappedText(
        text = note.content
    ).constrain {
        x = 7.5.pixels
        y = SiblingConstraint(padding = 2.5f)
        width = 100.percent - 7.5.pixels
        color = NoteablePalette.textFaded.toConstraint()
        textScale = 0.8.pixels
    } childOf background
}
