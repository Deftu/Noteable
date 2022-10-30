package xyz.deftu.noteable.gui.components

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import xyz.deftu.noteable.gui.NoteablePalette

class ButtonComponent(
    text: String
) : UIContainer() {
    private val background by UIBlock(NoteablePalette.background).constrain {
        width = 100.percent
        height = 100.percent
    } effect OutlineEffect(NoteablePalette.primary, 1f) childOf this
    private val textComponent by UIText(text).constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    } childOf background
}
