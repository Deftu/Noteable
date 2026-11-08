@file:OptIn(ExperimentalOneConfigUiApi::class, ExperimentalLayoutApi::class)

package dev.deftu.noteable.gui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.deftu.noteable.NoteActions
import dev.deftu.noteable.Noteable
import dev.deftu.noteable.PlayerContext
import dev.deftu.noteable.note.Note
import dev.deftu.noteable.note.NoteLibrary
import kotlinx.coroutines.delay
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.ui.v1.ExperimentalOneConfigUiApi
import org.polyfrost.oneconfig.api.ui.v1.components.Chip
import org.polyfrost.oneconfig.api.ui.v1.components.Icon
import org.polyfrost.oneconfig.api.ui.v1.components.IconButton
import org.polyfrost.oneconfig.api.ui.v1.components.Text
import org.polyfrost.oneconfig.api.ui.v1.components.Tooltip
import org.polyfrost.oneconfig.api.ui.v1.components.onClick
import org.polyfrost.oneconfig.api.ui.v1.components.rememberInteractionSource
import org.polyfrost.oneconfig.api.ui.v1.page.OneConfigPage
import org.polyfrost.oneconfig.api.ui.v1.page.PageGroup
import org.polyfrost.oneconfig.api.ui.v1.page.PageText
import org.polyfrost.oneconfig.api.ui.v1.page.currentPageSearchQuery
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

    /** Selects [note] the next time the page is shown, or clears the selection */
    fun focus(note: Note?) {
        selected = note?.uuid
    }

    @Composable
    override fun Content() = NotesScreen()
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

    Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (option in NoteFilter.entries) Chip(option.label, filter == option) { filter = option }
            Spacer(Modifier.weight(1f))
            Text(countLabel(shown.size, all.size), Modifier, theme.textColorSecondary, 12.sp)
            Tooltip({ TooltipText("Open notes folder") }, Modifier, Alignment.BottomCenter) {
                IconOnlyButton("folder") { NoteActions.openFolder() }
            }
            SmallButton("New note", "plus", hierarchy = ButtonHierarchy.PRIMARY) {
                NotesPage.focus(NoteActions.create())
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.weight(1f).fillMaxHeight()) {
                when {
                    all.isEmpty() -> EmptyState("No notes yet. Create one with New note.")
                    shown.isEmpty() -> EmptyState(if (filter == NoteFilter.PINNED && query.isEmpty()) "No notes are pinned to your HUD." else "No notes match that search.")
                    else -> NoteGrid(shown, pinned, selected?.uuid) { NotesPage.selected = if (NotesPage.selected == it.uuid) null else it.uuid }
                }
            }
            // Always laid out, so opening a note never reflows the grid
            if (selected != null) {
                EditorPane(selected, selected.uuid in pinned) { NotesPage.selected = null }
            } else {
                EditorPlaceholder(all.isNotEmpty())
            }
        }
    }
}

@Composable
private fun NoteGrid(notes: List<Note>, pinned: Set<UUID>, selected: UUID?, onSelect: (Note) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(CARD_MIN_WIDTH),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(notes, key = { it.uuid }) { note ->
            NoteCard(note, note.uuid in pinned, note.uuid == selected) { onSelect(note) }
        }
    }
}

@Composable
private fun NoteCard(note: Note, pinned: Boolean, selected: Boolean, onClick: () -> Unit) {
    val theme = OneConfigTheme.current
    val interaction = rememberInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    val border by animateColorAsState(
        when {
            selected -> OneConfigTheme.accent
            hovered -> theme.textColorSecondary.copy(alpha = 0.5f)
            else -> theme.borderColor
        },
    )

    Column(
        Modifier
            .fillMaxWidth()
            .height(CARD_HEIGHT)
            .clip(theme.modCardShape)
            .background(theme.modCardBackground, theme.modCardShape)
            .border(1.dp, border, theme.modCardShape)
            .hoverable(interaction)
            .pointerHoverIcon(PointerIcon.Hand)
            .onClick(interaction, onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(note.icon.ifEmpty { Noteable.ICON }, theme.textColor, Modifier.size(16.dp))
            ClippedText(note.displayTitle, Modifier.weight(1f), theme.textColor, 14.sp, FontWeight.Medium, 1)
        }
        ClippedText(
            note.content.ifBlank { "No content" },
            Modifier.weight(1f),
            theme.textColorSecondary,
            12.sp,
            FontWeight.Normal,
            3,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(timeLabel(note.updatedAt), Modifier.weight(1f), theme.textColorSecondary, 11.sp)
            if (pinned) StatusTag("Pinned")
        }
    }
}

@Composable
private fun EditorPlaceholder(hasNotes: Boolean) {
    val theme = OneConfigTheme.current
    Column(
        Modifier.editorFrame().padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            if (hasNotes) "Select a note to edit it." else "Notes you create open here.",
            Modifier,
            theme.textColorSecondary,
            12.sp,
        )
    }
}

/** The config page's list of pinned notes */
@Composable
fun PinnedNotesList() {
    val theme = OneConfigTheme.current
    val revision = NoteHuds.revision
    val pinned = remember(revision) { NoteHuds.pinnedNotes() }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (pinned.isEmpty()) {
            Text("No notes are pinned. Pin one from the Notes page.", Modifier.padding(vertical = 4.dp), theme.textColorSecondary, 12.sp)
        }
        for (note in pinned) {
            Row(
                Modifier.fillMaxWidth().background(theme.componentBackground, theme.buttonShape).padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ClippedText(note.displayTitle, Modifier.weight(1f), theme.textColor, 12.sp, FontWeight.Medium, 1)
                SmallButton("Edit", null) { NoteActions.openNotes(note) }
                SmallButton("Unpin", null) { NoteActions.setPinned(note, false) }
            }
        }
        SmallButton("Open notes", "text") { NoteActions.openNotes() }
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

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (notes.isEmpty()) {
            Text("You have no notes yet. Create one from the Notes page.", Modifier.padding(vertical = 4.dp), theme.textColorSecondary, 12.sp)
        }
        for (note in notes) {
            val id = note.uuid.toString()
            val selected = id == current
            val interaction = rememberInteractionSource()
            val hovered by interaction.collectIsHoveredAsState()
            val border by animateColorAsState(
                when {
                    selected -> OneConfigTheme.accent
                    hovered -> theme.textColorSecondary.copy(alpha = 0.6f)
                    else -> theme.borderColor
                },
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(theme.componentBackground, theme.buttonShape)
                    .border(1.dp, border, theme.buttonShape)
                    .hoverable(interaction)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .onClick(interaction) {
                        @Suppress("UNCHECKED_CAST")
                        (prop as Property<String>).set(id)
                    }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ClippedText(note.displayTitle, Modifier.weight(1f), theme.textColor, 12.sp, FontWeight.Medium, 1)
                if (selected) Icon("tick", OneConfigTheme.accent, Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    val theme = OneConfigTheme.current
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Noteable.ICON, theme.textColorSecondary, Modifier.size(32.dp))
        Spacer(Modifier.height(12.dp))
        Text(message, Modifier, theme.textColorSecondary, 14.sp)
    }
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
