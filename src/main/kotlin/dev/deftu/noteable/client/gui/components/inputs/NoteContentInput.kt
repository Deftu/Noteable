package dev.deftu.noteable.client.gui.components.inputs

import dev.deftu.noteable.NoteableConstants
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UIMultilineTextInput
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import dev.deftu.noteable.client.gui.NoteablePalette
import dev.deftu.textualizer.minecraft.MCLocalizedTextHolder

class NoteContentInput : UIContainer() {
    private val contentInputText by UIText(MCLocalizedTextHolder("gui.${NoteableConstants.ID}.text.modal.input.content.header").asString()).constrain {
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
        placeholder = MCLocalizedTextHolder("gui.${NoteableConstants.ID}.text.modal.input.content.input").asString(),
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

        contentInputBox.onMouseClick {
            contentInput.grabWindowFocus()
            it.stopImmediatePropagation()
        }.onFocus {
            contentInput.setActive(true)
        }.onFocusLost {
            contentInput.setActive(false)
        }
    }

    fun setText(text: String) = contentInput.setText(text)
    fun getText() = contentInput.getText()
}
