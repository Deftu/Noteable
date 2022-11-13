package xyz.deftu.noteable.gui.inputs

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import net.minecraft.client.resource.language.I18n
import xyz.deftu.lib.client.gui.DeftuPalette
import xyz.deftu.noteable.Noteable

class NoteTitleInput : UIContainer() {
    private val titleInputText by UIText(I18n.translate("${Noteable.ID}.menu.input.title.header")).constrain {
        textScale = 1.6.pixels
        color = DeftuPalette.getText().toConstraint()
    } childOf this
    private val titleInputBox by UIBlock(DeftuPalette.getBackground()).constrain {
        y = SiblingConstraint(2.5f)
        width = 260.pixels
        height = 25.pixels
    } effect OutlineEffect(
        color = DeftuPalette.getPrimary(),
        width = 1f
    ) childOf this
    private val titleInput by UITextInput(
        placeholder = I18n.translate("${Noteable.ID}.menu.input.title.input"),
        selectionForegroundColor = DeftuPalette.getPrimary(),
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
