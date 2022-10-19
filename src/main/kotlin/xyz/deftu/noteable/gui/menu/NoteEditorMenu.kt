package xyz.deftu.noteable.gui.menu

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.state.BasicState
import xyz.deftu.noteable.events.NoteConfigureEvent
import xyz.deftu.noteable.notes.NoteManager
import xyz.deftu.noteable.gui.NoteablePalette
import xyz.deftu.noteable.gui.components.ButtonComponent
import xyz.deftu.noteable.gui.components.NoteComponent
import xyz.deftu.noteable.gui.components.NoteCreateModalComponent

class NoteEditorMenu : WindowScreen(
    version = ElementaVersion.V2,
    restoreCurrentGuiOnClose = true,
    drawDefaultBackground = true
) {
    private val deleteState = BasicState(false)
    private val moveState = BasicState(false)
    private val editState = BasicState(false)
    private val pinState = BasicState(false)

    private val noteContainer by UIContainer().constrain {
        width = 100.percent
        height = 100.percent
    } childOf window

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
        val modal = NoteCreateModalComponent() childOf window
        modal.setReloader(::reload)
    } childOf buttonContainer

    private val deleteButton by ButtonComponent("Delete").constrain {
        x = SiblingConstraint(7.5f)
        width = 50.pixels
        height = 25.pixels
    }.onMouseClick {
        deleteState.set(!deleteState.get())
    } childOf buttonContainer

    private val moveButton by ButtonComponent("Move").constrain {
        x = SiblingConstraint(7.5f)
        width = 50.pixels
        height = 25.pixels
    }.onMouseClick {
        moveState.set(!moveState.get())
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

    private val deleteText by UIText("Delete Mode Activated").constrain {
        x = 7.5.pixels(true)
        y = 7.5.pixels
        textScale = 1.5.pixels
    } childOf window
    private val moveText by UIText("Move Mode Activated").constrain {
        x = 7.5.pixels(true)
        y = 7.5.pixels
        textScale = 1.5.pixels
    } childOf window
    private val editText by UIText("Edit Mode Activated").constrain {
        x = 7.5.pixels(true)
        y = 7.5.pixels
        textScale = 1.5.pixels
    } childOf window
    private val pinText by UIText("Pin Mode Activated").constrain {
        x = 7.5.pixels(true)
        y = 7.5.pixels
        textScale = 1.5.pixels
    } childOf window

    init {
        deleteText.hide(true)
        moveText.hide(true)
        editText.hide(true)
        pinText.hide(true)

        deleteState.onSetValue { value ->
            if (value) {
                moveState.set(false)
                editState.set(false)
                pinState.set(false)
                deleteText.unhide(true)
            } else deleteText.hide(true)
        }

        moveState.onSetValue { value ->
            if (value) {
                deleteState.set(false)
                editState.set(false)
                pinState.set(false)
                moveText.unhide(true)
            } else moveText.hide(true)
        }

        editState.onSetValue { value ->
            if (value) {
                deleteState.set(false)
                moveState.set(false)
                pinState.set(false)
                editText.unhide(true)
            } else editText.hide(true)
        }

        pinState.onSetValue { value ->
            if (value) {
                deleteState.set(false)
                moveState.set(false)
                editState.set(false)
                pinText.unhide(true)
            } else pinText.hide(true)
        }

        reload()
    }

    private fun reload() {
        noteContainer.clearChildren()
        NoteManager.getNotes().forEach { note ->
            val container by UIContainer().constrain {
                x = note.x.percent
                y = note.y.percent
                width = ChildBasedSizeConstraint()
                height = ChildBasedSizeConstraint()
            } effect OutlineEffect(
                color = NoteablePalette.primary,
                width = 1f
            ) childOf noteContainer
            val pinHandler = NotePinHandler()
            val noteComponent by NoteComponent(note).constrain {
                y = SiblingConstraint()
            }.apply {
                val deleteHandler = NoteDeleteHandler(this)
                deleteHandler.setDeleteState(deleteState)
                deleteHandler.setReloader(::reload)

                val moveHandler = NoteMoveHandler(this)
                moveHandler.setMoveState(moveState)

                val editHandler = NoteEditHandler(window, this)
                editHandler.setEditState(editState)
                editHandler.setReloader(::reload)

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
            } childOf noteComponent.header
            pinHandler.setPinComponent(pinComponent)
        }
    }

    override fun onScreenClose() {
        NoteManager.save()
        NoteConfigureEvent.EVENT.invoker().onNoteConfigure(NoteManager.getNotes())
        super.onScreenClose()
    }
}
