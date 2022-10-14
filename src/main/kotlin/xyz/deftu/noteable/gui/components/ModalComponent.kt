package xyz.deftu.noteable.gui.components

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*

abstract class ModalComponent : UIContainer() {
    init {
        constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        }
    }

    open fun postInitialize() {}

    override fun afterInitialization() {
        setFloating(true)
        postInitialize()
        super.afterInitialization()
    }
}
