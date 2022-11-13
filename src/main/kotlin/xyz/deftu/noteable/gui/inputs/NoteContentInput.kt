package xyz.deftu.noteable.gui.inputs

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UIMultilineTextInput
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import net.minecraft.client.resource.language.I18n
import xyz.deftu.lib.client.gui.DeftuPalette
import xyz.deftu.noteable.Noteable

class NoteContentInput : UIContainer() {
    private val contentInputText by UIText(I18n.translate("${Noteable.ID}.menu.input.content.header")).constrain {
        textScale = 1.6.pixels
        color = DeftuPalette.getText().toConstraint()
    } childOf this
    private val contentInputBox by UIBlock(DeftuPalette.getBackground()).constrain {
        y = SiblingConstraint(2.5f)
        width = 260.pixels
        height = 50.pixels
    } effect OutlineEffect(
        color = DeftuPalette.getPrimary(),
        width = 1f
    ) childOf this
    private val contentInput by UIMultilineTextInput(
        placeholder = I18n.translate("${Noteable.ID}.menu.input.content.input"),
        selectionForegroundColor = DeftuPalette.getPrimary(),
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
