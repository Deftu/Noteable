package xyz.deftu.noteable.gui.components

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UIMultilineTextInput
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import xyz.deftu.noteable.gui.NoteablePalette
import xyz.deftu.noteable.notes.Note
import xyz.deftu.noteable.notes.NoteManager
import java.util.UUID

class NoteCreateModalComponent : ModalComponent() {
    init {
        constrain {
            width = 275.pixels
            height = 190.pixels
        }

        val background by UIBlock(NoteablePalette.background).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent
            height = 100.percent
        } effect OutlineEffect(
            color = NoteablePalette.primary,
            width = 1f
        ) childOf this

        val title by UIText("Create new note").constrain {
            x = 7.5.pixels
            y = 7.5.pixels
            textScale = 1.9.pixels
        } childOf background
        val closeButton by UIText("X").constrain {
            x = 7.5.pixels(true)
            y = 7.5.pixels
        }.onMouseClick {
            this@NoteCreateModalComponent.hide(true)
        } childOf background

        val titleInput by TitleInput().constrain {
            x = 7.5.pixels
            y = SiblingConstraint(7.5f)
            width = 100.percent - 7.5.pixels
            height = ChildBasedSizeConstraint(2.5f)
        } childOf background
        val contentInput by ContentInput().constrain {
            x = 7.5.pixels
            y = SiblingConstraint(7.5f)
            width = 100.percent - 7.5.pixels
            height = ChildBasedSizeConstraint(2.5f)
        } childOf background

        val finishButton by ButtonComponent("Finish").constrain {
            x = CenterConstraint()
            y = SiblingConstraint(7.5f)
            width = 75.pixels
            height = 25.pixels
        }.onMouseClick {
            NoteManager.addNote(Note(
                uuid = UUID.randomUUID(),
                title = titleInput.getText(),
                content = contentInput.getText(),
                sticky = false,
                x = 50,
                y = 50
            ))
            this@NoteCreateModalComponent.hide()
        } childOf background
    }

    class TitleInput : UIContainer() {
        private val titleInputText by UIText("Title:").constrain {
            textScale = 1.6.pixels
        } childOf this
        private val titleInputBox by UIBlock(NoteablePalette.background).constrain {
            y = SiblingConstraint(2.5f)
            width = 260.pixels
            height = 25.pixels
        } effect OutlineEffect(
            color = NoteablePalette.primary,
            width = 1f
        ) childOf this
        private val titleInput by UITextInput(
            placeholder = "Title...",
            selectionForegroundColor = NoteablePalette.primary
        ).constrain {
            x = 2.5.pixels
            y = 2.5.pixels
            width = 100.percent - 2.5.pixels
            height = 100.percent - 2.5.pixels
        } childOf titleInputBox

        init {
            val width = 100.percent - 2.5.pixels
            titleInput.setMinWidth(width)
            titleInput.setMaxWidth(width)

            titleInput.onMouseClick {
                titleInput.grabWindowFocus()
            }.onFocus {
                titleInput.setActive(true)
            }.onFocusLost {
                titleInput.setActive(false)
            }
        }

        fun getText() = titleInput.getText()
    }

    class ContentInput : UIContainer() {
        private val contentInputText by UIText("Content:").constrain {
            textScale = 1.6.pixels
        } childOf this
        private val contentInputBox by UIBlock(NoteablePalette.background).constrain {
            y = SiblingConstraint(2.5f)
            width = 260.pixels
            height = 50.pixels
        } effect OutlineEffect(
            color = NoteablePalette.primary,
            width = 1f
        ) childOf this
        private val contentInput by UIMultilineTextInput(
            placeholder = "Content...",
            selectionForegroundColor = NoteablePalette.primary
        ).constrain {
            x = 2.5.pixels
            y = 2.5.pixels
            width = 100.percent - 2.5.pixels
            height = 100.percent - 2.5.pixels
        } childOf contentInputBox

        init {
            val height = 100.percent - 2.5.pixels
            contentInput.setMaxHeight(height)

            contentInput.onMouseClick {
                contentInput.grabWindowFocus()
            }.onFocus {
                contentInput.setActive(true)
            }.onFocusLost {
                contentInput.setActive(false)
            }
        }

        fun getText() = contentInput.getText()
    }
}
