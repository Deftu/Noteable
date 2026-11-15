@file:OptIn(ExperimentalOneConfigUiApi::class)

package dev.deftu.noteable.gui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.deftu.noteable.NoteActions
import dev.deftu.noteable.Noteable
import dev.deftu.noteable.note.Note
import dev.deftu.noteable.note.NoteLibrary
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.ui.v1.ExperimentalOneConfigUiApi
import org.polyfrost.oneconfig.api.ui.v1.OneConfigUI
import org.polyfrost.oneconfig.api.ui.v1.components.Badge
import org.polyfrost.oneconfig.api.ui.v1.components.Card
import org.polyfrost.oneconfig.api.ui.v1.components.CardGrid
import org.polyfrost.oneconfig.api.ui.v1.components.Chip
import org.polyfrost.oneconfig.api.ui.v1.components.Confirm
import org.polyfrost.oneconfig.api.ui.v1.components.ControlSize
import org.polyfrost.oneconfig.api.ui.v1.components.ControlStyle
import org.polyfrost.oneconfig.api.ui.v1.components.EmptyState
import org.polyfrost.oneconfig.api.ui.v1.components.Icon
import org.polyfrost.oneconfig.api.ui.v1.components.IconButton
import org.polyfrost.oneconfig.api.ui.v1.components.MasterDetailLayout
import org.polyfrost.oneconfig.api.ui.v1.components.MenuAction
import org.polyfrost.oneconfig.api.ui.v1.components.MenuEntry
import org.polyfrost.oneconfig.api.ui.v1.components.MenuSeparator
import org.polyfrost.oneconfig.api.ui.v1.components.RadioGroup
import org.polyfrost.oneconfig.api.ui.v1.components.SurfaceKind
import org.polyfrost.oneconfig.api.ui.v1.components.Text
import org.polyfrost.oneconfig.api.ui.v1.components.TextButton
import org.polyfrost.oneconfig.api.ui.v1.components.contextMenu
import org.polyfrost.oneconfig.api.ui.v1.components.themedSurface
import org.polyfrost.oneconfig.api.ui.v1.page.OneConfigPage
import org.polyfrost.oneconfig.api.ui.v1.page.PageGroup
import org.polyfrost.oneconfig.api.ui.v1.page.PageText
import org.polyfrost.oneconfig.api.ui.v1.page.currentPageSearchQuery
import org.polyfrost.oneconfig.api.ui.v1.page.resolve
import org.polyfrost.oneconfig.api.ui.v1.theme.OneConfigTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

private val CARD_MIN_WIDTH = 220.dp
private val CARD_HEIGHT = 116.dp


object NotesPage : OneConfigPage(
    "noteable:notes",
    PageText.translatable("noteable.page.notes", "Notes"),
    Noteable.ICON,
    PageGroup.MODS_AND_OPTIONS,
) {
    override val order: Int get() = 410

    override val searchPlaceholder: PageText = PageText.translatable("noteable.page.notes.search", "Search notes...")

    internal var selected by mutableStateOf<UUID?>(null)

    private var handledFocus: Focus? = null

    class Focus(val note: UUID?)

    @Composable
    override fun Content() {
        val focus = OneConfigUI.currentPageArgument as? Focus
        LaunchedEffect(focus) {
            if (focus != null && focus !== handledFocus) {
                handledFocus = focus
                selected = focus.note
            }
        }
        NotesScreen()
    }
}

private object NoteText {
    val OPEN = PageText.translatable("noteable.menu.open", "Open")
    val PIN = PageText.translatable("noteable.menu.pin", "Pin to HUD")
    val UNPIN = PageText.translatable("noteable.menu.unpin", "Unpin")
    val COPY = PageText.translatable("noteable.menu.copy", "Copy")
    val DUPLICATE = PageText.translatable("noteable.menu.duplicate", "Duplicate")
    val DELETE = PageText.translatable("noteable.menu.delete", "Delete")
    val DELETE_TITLE = PageText.translatable("noteable.delete.title", "Delete note?")
    val DELETE_TEXT = PageText.translatable("noteable.delete.text", "This can't be undone.")
    val DELETE_CONFIRM = PageText.translatable("noteable.delete.confirm", "Delete note")
    val NO_PINNED = PageText.translatable("noteable.pinned.empty", "No notes are pinned. Pin one from the Notes page.")
    val NO_NOTES_TO_PICK = PageText.translatable("noteable.picker.empty", "You have no notes yet. Create one from the Notes page.")
}

private enum class NoteFilter(val label: String) {
    ALL("All"),
    PINNED("Pinned"),
}

@Composable
private fun NotesScreen() {
    val theme = OneConfigTheme.current
    val revision = NoteLibrary.revision
    var filter by remember { mutableStateOf(NoteFilter.ALL) }
    val query = currentPageSearchQuery.trim()

    val hudRevision = NoteHuds.revision
    val all = remember(revision) { NoteLibrary.all() }
    val pinned = remember(hudRevision) { NoteHuds.pinnedNotes().map { it.uuid }.toSet() }
    val shown = remember(all, pinned, filter, query) {
        all.filter { note ->
            (filter == NoteFilter.ALL || note.uuid in pinned) &&
                (query.isEmpty() || note.title.contains(query, ignoreCase = true) || note.content.contains(query, ignoreCase = true))
        }
    }
    val selected = NotesPage.selected?.let { uuid -> all.firstOrNull { it.uuid == uuid } }

    val spacing = theme.spacing
    Column(Modifier.fillMaxSize().padding(start = spacing.xxl, end = spacing.xxl, top = spacing.xl)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.m)) {
            for (option in NoteFilter.entries) Chip(option.label, filter == option, { filter = option })
            Spacer(Modifier.weight(1f))
            Text(countLabel(shown.size, all.size), Modifier, theme.textColorSecondary, OneConfigTheme.typography.small)
            IconButton("folder", "Open notes folder", NoteActions::openFolder)
            TextButton("New note", "plus", { NotesPage.selected = NoteActions.create().uuid }, style = ControlStyle.Primary)
        }

        Spacer(Modifier.height(spacing.xl))

        MasterDetailLayout(
            list = {
                when {
                    all.isEmpty() -> EmptyState("No notes yet. Create one with New note.", Modifier.fillMaxSize(), icon = Noteable.ICON)
                    shown.isEmpty() -> EmptyState(
                        if (filter == NoteFilter.PINNED && query.isEmpty()) "No notes are pinned to your HUD." else "No notes match that search.",
                        Modifier.fillMaxSize(),
                        icon = Noteable.ICON,
                    )
                    else -> NoteGrid(shown, pinned, selected?.uuid) { NotesPage.selected = if (NotesPage.selected == it.uuid) null else it.uuid }
                }
            },
            detail = {
                // Always laid out, so opening a note never reflows the grid
                if (selected != null) {
                    EditorPane(selected, selected.uuid in pinned) { NotesPage.selected = null }
                } else {
                    EditorPlaceholder(all.isNotEmpty())
                }
            },
            detailWidth = EDITOR_WIDTH,
        )
    }
}

@Composable
private fun NoteGrid(notes: List<Note>, pinned: Set<UUID>, selected: UUID?, onSelect: (Note) -> Unit) {
    CardGrid(notes, key = { it.uuid }, Modifier.fillMaxSize(), minCellWidth = CARD_MIN_WIDTH) { note ->
        NoteCard(note, note.uuid in pinned, note.uuid == selected) { onSelect(note) }
    }
}

@Composable
private fun NoteCard(note: Note, pinned: Boolean, selected: Boolean, onClick: () -> Unit) {
    val theme = OneConfigTheme.current
    val typography = OneConfigTheme.typography
    val deleteTitle = NoteText.DELETE_TITLE.resolve()
    val deleteText = NoteText.DELETE_TEXT.resolve()
    val deleteConfirm = NoteText.DELETE_CONFIRM.resolve()

    val spacing = theme.spacing

    Card(
        Modifier
            .fillMaxWidth()
            .height(CARD_HEIGHT)
            .contextMenu { noteMenu(note, pinned, deleteTitle, deleteText, deleteConfirm) },
        onClick,
        note.displayTitle,
        selected,
        contentPadding = PaddingValues(spacing.l),
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(spacing.s)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.m)) {
                Icon(note.icon.ifEmpty { Noteable.ICON }, null, Modifier.size(16.dp), theme.textColor)
                Text(note.displayTitle, Modifier.weight(1f), theme.textColor, typography.label, 1, TextOverflow.Ellipsis)
            }
            Text(note.content.ifBlank { "No content" }, Modifier.weight(1f), theme.textColorSecondary, typography.small, 3, TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(timeLabel(note.updatedAt), Modifier.weight(1f), theme.textColorSecondary, typography.caption)
                if (pinned) Badge("Pinned")
            }
        }
    }
}

private fun noteMenu(note: Note, pinned: Boolean, deleteTitle: String, deleteText: String, deleteConfirm: String): List<MenuEntry> = listOf(
    MenuAction(NoteText.OPEN, { NotesPage.selected = note.uuid }, icon = "text-input"),
    MenuAction(if (pinned) NoteText.UNPIN else NoteText.PIN, { NoteActions.setPinned(note, !pinned) }, icon = "hud"),
    MenuAction(NoteText.COPY, { NoteActions.copy(note) }, icon = "copy"),
    MenuAction(NoteText.DUPLICATE, { NotesPage.selected = NoteActions.duplicate(note).uuid }, icon = "copy-plus"),
    MenuSeparator,
    MenuAction(
        NoteText.DELETE,
        { Confirm.ask(deleteTitle, deleteText, deleteConfirm, onConfirm = { NoteActions.delete(note) }) },
        icon = "trash",
        danger = true,
    ),
)

@Composable
private fun EditorPlaceholder(hasNotes: Boolean) {
    EmptyState(
        if (hasNotes) "Select a note to edit it." else "Notes you create open here.",
        Modifier.editorFrame(),
    )
}

/** The config page's list of pinned notes */
@Composable
fun PinnedNotesList() {
    val theme = OneConfigTheme.current
    val revision = NoteHuds.revision
    val pinned = remember(revision) { NoteHuds.pinnedNotes() }
    val spacing = theme.spacing

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacing.s)) {
        if (pinned.isEmpty()) {
            Text(NoteText.NO_PINNED.resolve(), Modifier.padding(vertical = spacing.xs), theme.textColorSecondary, OneConfigTheme.typography.small)
        }
        for (note in pinned) {
            Row(
                Modifier.fillMaxWidth().themedSurface(SurfaceKind.Field).padding(start = spacing.l, end = spacing.xs, top = spacing.xs, bottom = spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.s),
            ) {
                Text(note.displayTitle, Modifier.weight(1f), theme.textColor, OneConfigTheme.typography.label, 1, TextOverflow.Ellipsis)
                TextButton("Edit", { NoteActions.openNotes(note) }, size = ControlSize.Compact)
                TextButton("Unpin", { NoteActions.setPinned(note, false) }, size = ControlSize.Compact)
            }
        }
        TextButton("Open notes", "text", { NoteActions.openNotes() })
    }
}

/** The note picker shown in a [NoteHud]'s settings */
@Composable
fun NotePicker(prop: Property<*>) {
    val theme = OneConfigTheme.current
    val revision = NoteLibrary.revision
    val notes = remember(revision) { NoteLibrary.all() }
    // Read every recomposition: the HUD's note can change from elsewhere, such as when the HUD is duplicated
    val current = (prop.get() as? String).orEmpty()

    if (notes.isEmpty()) {
        Text(NoteText.NO_NOTES_TO_PICK.resolve(), Modifier.padding(vertical = theme.spacing.xs), theme.textColorSecondary, OneConfigTheme.typography.small)
        return
    }
    RadioGroup<Note?>(
        notes.firstOrNull { it.uuid.toString() == current },
        notes,
        { note ->
            @Suppress("UNCHECKED_CAST")
            if (note != null) (prop as Property<String>).set(note.uuid.toString())
        },
        Modifier.fillMaxWidth(),
        label = { it?.displayTitle.orEmpty() },
        fillWidth = true,
    )
}


private val ZONE: ZoneId get() = ZoneId.systemDefault()
private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
private val DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.ENGLISH)
private val FULL_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm", Locale.ENGLISH)

internal fun timeLabel(millis: Long): String {
    val time = Instant.ofEpochMilli(millis).atZone(ZONE)
    return when (time.toLocalDate()) {
        LocalDate.now(ZONE) -> "Today, ${TIME_FORMAT.format(time)}"
        LocalDate.now(ZONE).minusDays(1) -> "Yesterday, ${TIME_FORMAT.format(time)}"
        else -> DATE_FORMAT.format(time)
    }
}

/** [timeLabel] for use mid-sentence, where only the relative days are lowercased */
internal fun editedLabel(millis: Long): String = timeLabel(millis).let { label ->
    if (label.startsWith("Today") || label.startsWith("Yesterday")) label.replaceFirstChar { it.lowercase() } else label
}

internal fun fullTimeLabel(millis: Long): String = FULL_FORMAT.format(Instant.ofEpochMilli(millis).atZone(ZONE))

private fun countLabel(shown: Int, total: Int): String = when {
    shown == total -> if (total == 1) "1 note" else "$total notes"
    else -> "$shown of $total"
}
