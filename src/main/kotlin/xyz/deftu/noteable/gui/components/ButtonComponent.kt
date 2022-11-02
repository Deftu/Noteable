package xyz.deftu.noteable.gui.components

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.State
import xyz.deftu.noteable.gui.NoteablePalette
import java.awt.Color

class ButtonComponent(
    text: String,
    type: State<ButtonType>
) : UIContainer() {
    constructor(
        text: String,
        type: ButtonType
    ) : this(text, BasicState(type))
    constructor(
        text: String
    ) : this(text, ButtonType.NORMAL)

    private val outline = OutlineEffect(type.getOrDefault(ButtonType.NORMAL).color, 1f)
    private val background by UIBlock(NoteablePalette.background).constrain {
        width = 100.percent
        height = 100.percent
    } effect outline childOf this
    private val textComponent by UIText(text).constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    } childOf background

    init {
        type.onSetValue {
            outline.color = it.color
        }
    }

    enum class ButtonType(
        val color: Color
    ) {
        NORMAL(NoteablePalette.primary),
        CANCEL(Color(201, 18, 18));

        companion object {
            fun Boolean.toButtonType() = if (this) NORMAL else CANCEL
        }
    }
}
