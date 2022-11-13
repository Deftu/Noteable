package xyz.deftu.noteable.gui

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
    open fun postClose() {}

    override fun afterInitialization() {
        setFloating(true)
        postInitialize()
        super.afterInitialization()
    }

    fun close() {
        setFloating(false)
        hide(true)
        postClose()
    }
}
