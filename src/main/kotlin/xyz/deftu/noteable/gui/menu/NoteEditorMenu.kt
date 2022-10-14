package xyz.deftu.noteable.gui.menu

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.state.BasicState
import gg.essential.universal.GuiScale
import xyz.deftu.noteable.events.NoteConfigureEvent
import xyz.deftu.noteable.notes.NoteManager
import xyz.deftu.noteable.gui.NoteablePalette
import xyz.deftu.noteable.gui.components.ButtonComponent
import xyz.deftu.noteable.gui.components.NoteComponent
import xyz.deftu.noteable.gui.components.NoteCreateModalComponent

class NoteEditorMenu : WindowScreen(
    version = ElementaVersion.V2,
    restoreCurrentGuiOnClose = true,
    drawDefaultBackground = true,
    newGuiScale = GuiScale.scaleForScreenSize().ordinal
) {
    private val editState = BasicState(false)
    private val pinState = BasicState(false)

    private val buttonContainer by UIContainer().constrain {
        x = 7.5.pixels
        y = 7.5.pixels
        width = ChildBasedSizeConstraint(7.5f)
        height = 25.pixels
    } childOf window

    private val addButton by ButtonComponent("Add").constrain {
        width = 50.pixels
        height = 25.pixels
    }.onMouseClick {
        NoteCreateModalComponent() childOf window
    } childOf buttonContainer

    private val editButton by ButtonComponent("Edit").constrain {
        x = SiblingConstraint(7.5f)
        width = 50.pixels
        height = 25.pixels
    }.onMouseClick {
        editState.set(!editState.get())
    } childOf buttonContainer

    private val pinButton by ButtonComponent("Pin").constrain {
        x = SiblingConstraint(7.5f)
        width = 50.pixels
        height = 25.pixels
    }.onMouseClick {
        pinState.set(!pinState.get())
    } childOf buttonContainer

    private val editText by UIText("Edit Mode Activated").constrain {
        x = 7.5.pixels(true)
        y = 7.5.pixels
        textScale = 1.5.pixels
    } childOf window
    private val pinText by UIText("Pin Mode Activated").constrain {
        x = 7.5.pixels(true)
        y = 15.pixels
        textScale = 1.5.pixels
    } childOf window

    init {
        Inspector(window) childOf window

        editText.hide(true)
        pinText.hide(true)

        editState.onSetValue { value ->
            pinState.set(false)
            if (value) editText.unhide(true) else editText.hide(true)
        }

        pinState.onSetValue { value ->
            editState.set(false)
            if (value) pinText.unhide(true) else pinText.hide(true)
        }

        NoteManager.getNotes().forEach { note ->
            val container by UIContainer().constrain {
                x = note.x.percent
                y = note.y.percent
                width = ChildBasedSizeConstraint()
                height = ChildBasedSizeConstraint()
            } effect OutlineEffect(
                color = NoteablePalette.primary,
                width = 1f
            ) childOf window
            val pinHandler = NotePinHandler()
            val noteComponent by NoteComponent(note).constrain {
                y = SiblingConstraint()
            }.apply {
                val dragHandler = NoteDragHandler(this)
                dragHandler.setEditState(editState)

                pinHandler.initialize(this)
                pinHandler.setPinState(pinState)
            } effect OutlineEffect(
                color = NoteablePalette.primary,
                width = 1f
            ) childOf container
            val pinComponent by UIImage.ofResourceCached("/assets/noteable/pin.png").constrain {
                x = 7.5.pixels(true)
                y = 7.5.pixels
                width = 8.pixels
                height = 8.pixels
            } childOf noteComponent
            pinHandler.setPinComponent(pinComponent)
        }
    }

    override fun onScreenClose() {
        NoteManager.save()
        NoteConfigureEvent.EVENT.invoker().onNoteConfigure(NoteManager.getNotes())
        super.onScreenClose()
    }
}
