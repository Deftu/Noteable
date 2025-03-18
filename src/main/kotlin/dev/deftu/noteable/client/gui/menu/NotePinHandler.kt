package dev.deftu.noteable.client.gui.menu

import dev.deftu.noteable.client.ClientNoteManager
import dev.deftu.noteable.client.gui.components.NoteComponent
import gg.essential.elementa.UIComponent
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.State

class NotePinHandler {
    private lateinit var component: NoteComponent
    private lateinit var pinComponent: UIComponent
    private lateinit var pinState: State<Boolean>

    fun initialize(component: NoteComponent) {
        this.component = component
        component.onMouseClick {
            if (!pinState.get()) {
                return@onMouseClick
            }

            component.note.isSticky = !component.note.isSticky
            configurePinComponent()

            ClientNoteManager.update(component.note)
        }
    }

    private fun configurePinComponent() {
        if (component.note.isSticky) {
            component.title.constrain {
                width = 100.percent - 25.pixels
            }

            pinComponent.unhide(true)
        } else {
            pinComponent.hide(true)
            component.title.constrain {
                width = 100.percent - 15.pixels
            }
        }
    }

    fun setPinState(state: State<Boolean>) {
        pinState = state
    }

    fun setPinComponent(component: UIComponent) {
        pinComponent = component
        configurePinComponent()
    }
}
