package dev.deftu.noteable.client.gui.components

import dev.deftu.noteable.NoteableConstants
import dev.deftu.noteable.client.ClientNoteManager
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import net.minecraft.client.resource.language.I18n
import dev.deftu.noteable.client.gui.NoteablePalette
import dev.deftu.noteable.client.gui.components.inputs.NoteContentInput
import dev.deftu.noteable.client.gui.components.inputs.NoteTitleInput
import dev.deftu.noteable.note.Note
import dev.deftu.textile.minecraft.MCTranslatableTextHolder
import java.util.UUID

class NoteCreateModalComponent : ModalComponent() {

    private lateinit var reloader: () -> Unit

    init {
        constrain {
            width = 275.pixels
            height = 190.pixels
        }

        val background by UIBlock(NoteablePalette.background).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent
            height = 100.percent
        } effect OutlineEffect(
            color = NoteablePalette.primary,
            width = 1f
        ) childOf this

        val title by UIText(MCTranslatableTextHolder("gui.${NoteableConstants.ID}.text.modal.create").asString()).constrain {
            x = 7.5.pixels
            y = 7.5.pixels
            textScale = 1.9.pixels
        } childOf background
        val closeButton by UIText("X").constrain {
            x = 7.5.pixels(true)
            y = 7.5.pixels
        }.onMouseClick {
            close()
        } childOf background

        val titleInput by NoteTitleInput().constrain {
            x = 7.5.pixels
            y = SiblingConstraint(7.5f)
            width = 100.percent - 7.5.pixels
            height = ChildBasedSizeConstraint(2.5f)
        } childOf background
        val contentInput by NoteContentInput().constrain {
            x = 7.5.pixels
            y = SiblingConstraint(7.5f)
            width = 100.percent - 7.5.pixels
            height = ChildBasedSizeConstraint(2.5f)
        } childOf background

        val finishButton by ButtonComponent(I18n.translate("gui.done")).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(7.5f)
            width = 75.pixels
            height = 25.pixels
        }.onMouseClick {
            val title = titleInput.getText()
            val content = contentInput.getText()
            if (title.isBlank() || content.isBlank())
                return@onMouseClick

            ClientNoteManager.add(Note(
                uuid = UUID.randomUUID(),
                title = title,
                content = content,
                isSticky = false,
                isSyncable = true,
                x = 50,
                y = 50
            ))

            close()
        } childOf background
    }

    override fun postClose() {
        reloader()
    }

    fun setReloader(block: () -> Unit) {
        this.reloader = block
    }

}
