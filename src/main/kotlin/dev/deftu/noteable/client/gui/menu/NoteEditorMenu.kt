package dev.deftu.noteable.client.gui.menu

import dev.deftu.noteable.NoteableConstants
import dev.deftu.noteable.client.ClientNoteManager
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
import net.minecraft.client.resource.language.I18n
import dev.deftu.noteable.client.gui.NoteablePalette
import dev.deftu.noteable.client.gui.components.ButtonComponent
import dev.deftu.noteable.client.gui.components.NoteComponent
import dev.deftu.noteable.client.gui.components.NoteCreateModalComponent

class NoteEditorMenu : WindowScreen(
    version = ElementaVersion.V8,
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

    private val addButton by ButtonComponent(I18n.translate("gui.${NoteableConstants.ID}.buttons.notes.add")).constrain {
        width = 50.pixels
        height = 25.pixels
    }.onMouseClick {
        val modal = NoteCreateModalComponent() childOf window
        modal.setReloader(::reload)
    } childOf buttonContainer

    private val deleteButton by ButtonComponent(I18n.translate("gui.${NoteableConstants.ID}.buttons.notes.delete")).constrain {
        x = SiblingConstraint(7.5f)
        width = 50.pixels
        height = 25.pixels
    }.onMouseClick {
        deleteState.set(!deleteState.get())
    } childOf buttonContainer

    private val moveButton by ButtonComponent(I18n.translate("gui.${NoteableConstants.ID}.buttons.notes.move")).constrain {
        x = SiblingConstraint(7.5f)
        width = 50.pixels
        height = 25.pixels
    }.onMouseClick {
        moveState.set(!moveState.get())
    } childOf buttonContainer

    private val editButton by ButtonComponent(I18n.translate("gui.${NoteableConstants.ID}.buttons.notes.edit")).constrain {
        x = SiblingConstraint(7.5f)
        width = 50.pixels
        height = 25.pixels
    }.onMouseClick {
        editState.set(!editState.get())
    } childOf buttonContainer

    private val pinButton by ButtonComponent(I18n.translate("gui.${NoteableConstants.ID}.buttons.notes.pin")).constrain {
        x = SiblingConstraint(7.5f)
        width = 50.pixels
        height = 25.pixels
    }.onMouseClick {
        pinState.set(!pinState.get())
    } childOf buttonContainer

    private val deleteText by UIText(I18n.translate("gui.${NoteableConstants.ID}.text.notes.delete")).constrain {
        x = 7.5.pixels(true)
        y = 7.5.pixels
        textScale = 1.5.pixels
    } childOf window
    private val moveText by UIText(I18n.translate("gui.${NoteableConstants.ID}.text.notes.move")).constrain {
        x = 7.5.pixels(true)
        y = 7.5.pixels
        textScale = 1.5.pixels
    } childOf window
    private val editText by UIText(I18n.translate("gui.${NoteableConstants.ID}.text.notes.edit")).constrain {
        x = 7.5.pixels(true)
        y = 7.5.pixels
        textScale = 1.5.pixels
    } childOf window
    private val pinText by UIText(I18n.translate("gui.${NoteableConstants.ID}.text.notes.pin")).constrain {
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
        ClientNoteManager.get().forEach { note ->
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
        ClientNoteManager.updateAll()
        super.onScreenClose()
    }
}
