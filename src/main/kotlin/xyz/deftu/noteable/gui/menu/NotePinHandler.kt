package xyz.deftu.noteable.gui.menu

import gg.essential.elementa.UIComponent
import gg.essential.elementa.state.State
import xyz.deftu.noteable.gui.components.NoteComponent

class NotePinHandler {
    private lateinit var component: NoteComponent
    private lateinit var pinComponent: UIComponent
    private lateinit var pinState: State<Boolean>

    fun initialize(component: NoteComponent) {
        this.component = component
        component.onMouseClick {
            if (!pinState.get())
                return@onMouseClick

            component.note.sticky = !component.note.sticky
            configurePinComponent()
        }
    }

    private fun configurePinComponent() {
        if (component.note.sticky) pinComponent.unhide(true) else pinComponent.hide(true)
    }

    fun setPinState(state: State<Boolean>) {
        pinState = state
    }

    fun setPinComponent(component: UIComponent) {
        pinComponent = component
        configurePinComponent()
    }
}
