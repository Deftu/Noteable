package xyz.deftu.noteable.gui

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import xyz.deftu.lib.client.gui.DeftuPalette

class ButtonComponent(
    text: String
) : UIContainer() {
    private val outline = OutlineEffect(DeftuPalette.getButton(), 1f)
    private val background by UIBlock(DeftuPalette.getBackground()).constrain {
        width = 100.percent
        height = 100.percent
    } effect outline childOf this
    private val textComponent by UIText(text).constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    } childOf background
}
