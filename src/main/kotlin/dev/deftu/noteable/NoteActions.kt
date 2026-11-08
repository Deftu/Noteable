package dev.deftu.noteable

import dev.deftu.noteable.config.NoteableConfig
import dev.deftu.noteable.gui.NoteHuds
import dev.deftu.noteable.gui.NotesPage
import dev.deftu.noteable.note.Note
import dev.deftu.noteable.note.NoteLibrary
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.polyfrost.oneconfig.api.notifications.v1.NotificationAction
import org.polyfrost.oneconfig.api.notifications.v1.NotificationType
import org.polyfrost.oneconfig.api.notifications.v1.Notifications
import org.polyfrost.oneconfig.api.platform.v1.DesktopHelper
import org.polyfrost.oneconfig.api.platform.v1.Platform
import org.polyfrost.oneconfig.api.ui.v1.ExperimentalOneConfigUiApi
import org.polyfrost.oneconfig.api.ui.v1.OneConfigUI
import org.polyfrost.oneconfig.utils.v1.ClipboardHelper

object NoteActions {
    private const val PREVIEW_LENGTH = 200

    fun create(title: String = ""): Note {
        val note = NoteLibrary.create(title.trim())
        if (NoteableConfig.pinNewNotes) NoteHuds.pin(note)
        return note
    }

    @OptIn(ExperimentalOneConfigUiApi::class)
    fun openNotes(focus: Note? = null) {
        NotesPage.focus(focus)
        OneConfigUI.open(NotesPage)
    }

    fun announceCreated(note: Note) {
        Notifications.send(
            "Note created", note.displayTitle, NotificationType.SUCCESS,
            actions = listOf(NotificationAction("Edit", primary = true) { openNotes(note) }),
        )
    }

    fun setPinned(note: Note, pinned: Boolean) {
        if (pinned) {
            if (NoteHuds.pin(note) == null) {
                notifyError("Couldn't pin", "The HUD for ${note.displayTitle} couldn't be created.")
                return
            }
            Notifications.success("Pinned", "${note.displayTitle} is on your HUD. Move it in the HUD editor.")
        } else {
            NoteHuds.unpin(note)
            Notifications.success("Unpinned", "${note.displayTitle} is no longer on your HUD.")
        }
    }

    fun copy(note: Note) {
        val text = listOf(note.title, note.content).filter { it.isNotBlank() }.joinToString("\n\n")
        ClipboardHelper.setString(text)
        Notifications.success("Copied", "${note.displayTitle} is on your clipboard.")
    }

    fun delete(note: Note) {
        NoteHuds.unpin(note)
        if (NoteLibrary.delete(note.uuid)) Notifications.success("Deleted", "${note.displayTitle} was deleted.")
    }

    fun openFolder() {
        val dir = NoteLibrary.directory.apply { mkdirs() }
        if (!DesktopHelper.open(dir)) notifyError("Couldn't open", dir.absolutePath)
    }

    fun notifyError(title: String, message: String) {
        Notifications.error(title, message)
    }

    fun sendList() {
        val notes = NoteLibrary.all()
        val chat = Platform.compatibility()
        if (notes.isEmpty()) {
            chat.displayChatMessage(
                Component.text("You have no notes. ", NamedTextColor.GRAY)
                    .append(action("New note", NamedTextColor.GREEN) { it.clickEvent(ClickEvent.suggestCommand("/${Noteable.ID} new ")) }),
            )
            return
        }

        chat.displayChatMessage(Component.text("Notes (${notes.size})", NamedTextColor.WHITE).decorate(TextDecoration.BOLD))
        for (note in notes) {
            val command = "/${Noteable.ID}"
            val preview = note.content.take(PREVIEW_LENGTH).let { if (note.content.length > PREVIEW_LENGTH) "$it..." else it }
            val pinned = NoteHuds.isPinned(note)
            val title = Component.text(note.displayTitle, if (pinned) NamedTextColor.AQUA else NamedTextColor.WHITE)
                .hoverEvent(HoverEvent.showText(Component.text(preview.ifBlank { "No content" }, NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.runCommand("$command open ${note.uuid}"))
            chat.displayChatMessage(
                Component.text(" - ", NamedTextColor.DARK_GRAY)
                    .append(title)
                    .append(Component.text(" "))
                    .append(action(if (pinned) "Unpin" else "Pin", NamedTextColor.YELLOW) { it.clickEvent(ClickEvent.runCommand("$command pin ${note.uuid}")) })
                    .append(action("Copy", NamedTextColor.GREEN) { it.clickEvent(ClickEvent.runCommand("$command copy ${note.uuid}")) }),
            )
        }
    }

    // Adventure's ClickEvent is generic on newer versions only, so the event is attached by the caller instead of passed in
    private fun action(label: String, color: NamedTextColor, withClick: (Component) -> Component): Component =
        Component.text("[", NamedTextColor.DARK_GRAY)
            .append(withClick(Component.text(label, color).decorate(TextDecoration.BOLD)))
            .append(Component.text("] ", NamedTextColor.DARK_GRAY))
}
