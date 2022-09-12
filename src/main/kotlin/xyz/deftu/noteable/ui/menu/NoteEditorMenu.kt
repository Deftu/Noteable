package xyz.deftu.noteable.ui.menu

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.GuiScale
import xyz.deftu.noteable.notes.NoteManager
import xyz.deftu.noteable.ui.NoteablePalette
import xyz.deftu.noteable.ui.components.ButtonComponent
import xyz.deftu.noteable.ui.components.NoteComponent
import xyz.deftu.noteable.ui.components.NoteHeaderComponent

class NoteEditorMenu : WindowScreen(
    version = ElementaVersion.V2,
    restoreCurrentGuiOnClose = true,
    newGuiScale = GuiScale.scaleForScreenSize().ordinal
) {
    private val addButton by ButtonComponent("+").constrain {
        x = 7.5.pixels(true)
        y = 7.5.pixels
        width = 25.pixels
        height = 25.pixels
    }.onMouseClick {
        println("I'm supposed to be adding a note right now!")
    } childOf window

    init {
        NoteManager.getNotes().forEach { note ->
            val container by UIContainer().constrain {
                x = note.x.percent
                y = note.y.percent - NoteHeaderComponent.HEIGHT.pixels
                width = 200.pixels
                height = ChildBasedSizeConstraint()
            } effect OutlineEffect(
                color = NoteablePalette.primary,
                width = 1f
            ) childOf window
            val header by NoteHeaderComponent(note) effect OutlineEffect(color = NoteablePalette.primary, width = 1f, sides = setOf(OutlineEffect.Side.Bottom)) childOf container
            val component by NoteComponent(note).constrain {
                y = SiblingConstraint()
            } childOf container
        }
    }

    override fun onScreenClose() {
        NoteManager.save()
        super.onScreenClose()
    }
}
