package dev.deftu.noteable.client.gui.components.inputs

import dev.deftu.noteable.NoteableConstants
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import dev.deftu.noteable.client.gui.NoteablePalette
import dev.deftu.textualizer.minecraft.MCLocalizedTextHolder

class NoteTitleInput : UIContainer() {
    private val titleInputText by UIText(MCLocalizedTextHolder("gui.${NoteableConstants.ID}.text.modal.input.title.header").asString()).constrain {
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
        placeholder = MCLocalizedTextHolder("gui.${NoteableConstants.ID}.text.modal.input.title.input").asString(),
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

        titleInputBox.onMouseClick {
            titleInput.grabWindowFocus()
            it.stopImmediatePropagation()
        }.onFocus {
            titleInput.setActive(true)
        }.onFocusLost {
            titleInput.setActive(false)
        }
    }

    fun setText(text: String) = titleInput.setText(text)
    fun getText() = titleInput.getText()
}
