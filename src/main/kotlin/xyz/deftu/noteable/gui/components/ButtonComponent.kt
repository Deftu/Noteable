package xyz.deftu.noteable.gui.components

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.utils.withAlpha
import xyz.deftu.noteable.gui.NoteablePalette
import java.awt.Color

class ButtonComponent(
    text: String
) : UIContainer() {
    private val outline = OutlineEffect(NoteablePalette.primary, 1f)
    private val hoverState = BasicState(false)

    private val background by UIBlock(NoteablePalette.background).constrain {
        width = 100.percent
        height = 100.percent
    } effect outline childOf this
    private val textComponent by UIText(text).constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    } childOf background

    init {
        outline.color = Color.BLACK.withAlpha(0)
        hoverState.onSetValue { state ->
            outline::color.animate(Animations.OUT_EXP, 0.5f,
                if (state) NoteablePalette.primary else Color.BLACK.withAlpha(0))
        }

        onMouseEnter {
            hoverState.set(true)
        }.onMouseLeave {
            hoverState.set(false)
        }
    }
}
