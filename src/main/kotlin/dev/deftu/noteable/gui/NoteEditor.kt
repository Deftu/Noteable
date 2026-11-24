@file:OptIn(ExperimentalOneConfigUiApi::class, ExperimentalLayoutApi::class)

package dev.deftu.noteable.gui

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dev.deftu.noteable.NoteActions
import dev.deftu.noteable.Noteable
import dev.deftu.noteable.PlayerContext
import dev.deftu.noteable.note.Note
import dev.deftu.noteable.note.NoteLibrary
import org.polyfrost.compose.host.BackStack
import org.polyfrost.oneconfig.api.ui.v1.ExperimentalOneConfigUiApi
import org.polyfrost.oneconfig.api.ui.v1.components.ArmedConfirmButton
import org.polyfrost.oneconfig.api.ui.v1.components.Badge
import org.polyfrost.oneconfig.api.ui.v1.components.ControlSize
import org.polyfrost.oneconfig.api.ui.v1.components.ControlStyle
import org.polyfrost.oneconfig.api.ui.v1.components.Divider
import org.polyfrost.oneconfig.api.ui.v1.components.FieldState
import org.polyfrost.oneconfig.api.ui.v1.components.IconButton
import org.polyfrost.oneconfig.api.ui.v1.components.ScrollContainer
import org.polyfrost.oneconfig.api.ui.v1.components.SurfaceKind
import org.polyfrost.oneconfig.api.ui.v1.components.Text
import org.polyfrost.oneconfig.api.ui.v1.components.TextButton
import org.polyfrost.oneconfig.api.ui.v1.components.TextField
import org.polyfrost.oneconfig.api.ui.v1.components.TooltipPlacement
import org.polyfrost.oneconfig.api.ui.v1.components.fieldFrame
import org.polyfrost.oneconfig.api.ui.v1.components.isControllerMode
import org.polyfrost.oneconfig.api.ui.v1.components.rememberInteractionSource
import org.polyfrost.oneconfig.api.ui.v1.components.themedSurface
import org.polyfrost.oneconfig.api.ui.v1.theme.OneConfigTheme

internal val EDITOR_WIDTH = 360.dp

/** OneConfig's own glyphs, so each one has a Minecraft-theme counterpart; the first is Noteable's default */
internal val NOTE_ICONS = listOf(
    Noteable.ICON to "Noteable", "star" to "Star", "bell" to "Bell", "info" to "Info", "alert-circle" to "Alert",
    "check-circle" to "Check", "lock" to "Lock", "combat" to "Sword", "spanner" to "Spanner", "scissors" to "Scissors",
    "paintbrush" to "Paintbrush", "layers" to "Layers", "cloud" to "Cloud", "moon" to "Moon", "sun" to "Sun",
    "keyboard" to "Keyboard", "console" to "Console", "hypixel" to "Hypixel",
)

/** Something the editor can drop into a note at the cursor */
private class Insertion(val icon: String, val label: String, val missing: String?, val text: () -> String?)

private val CONTEXT_INSERTIONS = listOf(
    Insertion("move", "Coordinates", "Join a world to insert your coordinates.") { PlayerContext.coordinates() },
    Insertion("layers", "Dimension", "Join a world to insert your dimension.") { PlayerContext.dimension() },
    Insertion("sun", "Biome", "Join a world to insert your biome.") { PlayerContext.biome() },
    Insertion("right-arrow", "Facing", "Join a world to insert the way you're facing.") { PlayerContext.facing() },
    Insertion("cloud", "World or server", "Join a world or server to insert its name.") { PlayerContext.place() },
    Insertion("profiles", "Player name", null) { PlayerContext.playerName() },
    Insertion("bell", "Date and time", null) { PlayerContext.dateTime() },
)

private val FORMAT_INSERTIONS = listOf(
    Insertion("check-circle", "Checklist item", null) { "[ ] " },
    Insertion("align-left", "Bullet", null) { "- " },
    Insertion("minus", "Divider", null) { "---" },
)

private val LOCATION = Insertion("copy-plus", "Full location", "Join a world to insert your location.") {
    val coordinates = PlayerContext.coordinates() ?: return@Insertion null
    listOfNotNull(coordinates, PlayerContext.dimension(), PlayerContext.biome()).joinToString(" · ")
}

@Composable
internal fun Modifier.editorFrame(): Modifier {
    return fillMaxSize()
        .padding(bottom = OneConfigTheme.current.spacing.xxl)
        .themedSurface(SurfaceKind.Card)
}

@Composable
internal fun EditorPane(note: Note, pinned: Boolean, onClose: () -> Unit) {
    val theme = OneConfigTheme.current
    var title by remember(note.uuid) { mutableStateOf(note.title) }
    var content by remember(note.uuid) { mutableStateOf(TextFieldValue(note.content, TextRange(note.content.length))) }
    var pickingIcon by remember(note.uuid) { mutableStateOf(false) }
    val latestClose by rememberUpdatedState(onClose)

    if (isControllerMode()) {
        DisposableEffect(note.uuid) {
            val back = BackStack.register { latestClose() }
            onDispose { back.close() }
        }
    }

    // Ticking a checklist on the HUD edits the note from outside this editor
    LaunchedEffect(note.content) {
        if (note.content != content.text) {
            val cursor = content.selection.end.coerceAtMost(note.content.length)
            content = TextFieldValue(note.content, TextRange(cursor))
        }
    }

    fun setContent(next: TextFieldValue) {
        content = next
        NoteLibrary.edit(note.uuid, content = next.text)
    }

    fun insert(insertion: Insertion) {
        val text = insertion.text()
        if (text == null) {
            NoteActions.notifyError("Nothing to insert", insertion.missing ?: "")
            return
        }
        val value = content.text
        val start = content.selection.min
        val end = content.selection.max
        // Formatting starts on its own line unless the cursor already is at a line start; context inserts inline
        val needsBreak = insertion in FORMAT_INSERTIONS && start > 0 && value[start - 1] != '\n'
        val piece = (if (needsBreak) "\n" else "") + text
        setContent(TextFieldValue(value.substring(0, start) + piece + value.substring(end), TextRange(start + piece.length)))
    }

    val spacing = theme.spacing
    Column(Modifier.editorFrame()) {
        Column(Modifier.padding(start = spacing.l, end = spacing.m, top = spacing.l, bottom = spacing.l), verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.m)) {
                IconButton(note.icon.ifEmpty { Noteable.ICON }, "Change icon", { pickingIcon = !pickingIcon }, selected = pickingIcon)
                TextField(
                    title,
                    {
                        title = it
                        NoteLibrary.edit(note.uuid, title = it)
                    },
                    Modifier.weight(1f),
                    placeholder = "Untitled note",
                )
                IconButton("close", "Close", onClose, size = ControlSize.Compact)
            }
            Row(Modifier.padding(start = ControlSize.Regular.height + spacing.m), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.s)) {
                Text("Edited ${editedLabel(note.updatedAt)}", Modifier, theme.textColorSecondary, OneConfigTheme.typography.caption)
                if (pinned) Badge("Pinned")
            }
        }

        if (pickingIcon) {
            FlowRow(
                Modifier.fillMaxWidth().padding(start = spacing.l, end = spacing.l, bottom = spacing.l),
                horizontalArrangement = Arrangement.spacedBy(spacing.s),
                verticalArrangement = Arrangement.spacedBy(spacing.s),
            ) {
                for ((icon, label) in NOTE_ICONS) {
                    val value = if (icon == Noteable.ICON) "" else icon
                    IconButton(
                        icon,
                        "$label icon",
                        {
                            NoteLibrary.setIcon(note.uuid, value)
                            pickingIcon = false
                        },
                        selected = note.icon == value,
                        tooltip = null,
                    )
                }
            }
        }

        Divider()

        key(note.uuid) {
            ContentEditor(
                content,
                Modifier.weight(1f).fillMaxWidth().padding(spacing.l),
                onChange = ::setContent,
                onInsert = ::insert,
            )
        }

        Divider()

        EditorFooter(note, pinned, onClose)
    }
}

/** The content field with its insert toolbar attached to the top edge, so the two read as one control */
@Composable
private fun ContentEditor(
    value: TextFieldValue,
    modifier: Modifier,
    onChange: (TextFieldValue) -> Unit,
    onInsert: (Insertion) -> Unit,
) {
    val theme = OneConfigTheme.current
    val spacing = theme.spacing
    val interaction = rememberInteractionSource()
    val focused by interaction.collectIsFocusedAsState()
    val hovered by interaction.collectIsHoveredAsState()

    Column(
        modifier
            .hoverable(interaction)
            .fieldFrame(FieldState.of(hovered, focused)),
    ) {
        FlowRow(
            Modifier.fillMaxWidth().padding(spacing.s),
            horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            ToolbarButton(LOCATION, onInsert)
            ToolbarSeparator()
            for (insertion in CONTEXT_INSERTIONS) ToolbarButton(insertion, onInsert)
            ToolbarSeparator()
            for (insertion in FORMAT_INSERTIONS) ToolbarButton(insertion, onInsert)
        }
        Divider()
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            // The field fills the visible area so clicking below the text still focuses it
            val minHeight = (maxHeight - spacing.m * 2).coerceAtLeast(0.dp)
            ScrollContainer(rememberScrollState(), Modifier.fillMaxSize(), scrollbar = false) {
                Box(Modifier.fillMaxWidth().padding(horizontal = spacing.l, vertical = spacing.m)) {
                    TextField(
                        value,
                        onChange,
                        Modifier.fillMaxWidth().heightIn(min = minHeight),
                        placeholder = "Write something, or insert where you are from the toolbar.",
                        singleLine = false,
                        framed = false,
                        interactionSource = interaction,
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = spacing.l, vertical = spacing.s)) {
            Spacer(Modifier.weight(1f))
            val lines = if (value.text.isEmpty()) 0 else value.text.count { it == '\n' } + 1
            Text("${plural(lines, "line")} · ${plural(value.text.length, "character")}", Modifier, theme.textColorSecondary, OneConfigTheme.typography.caption)
        }
    }
}

@Composable
private fun ToolbarButton(insertion: Insertion, onInsert: (Insertion) -> Unit) {
    IconButton(insertion.icon, insertion.label, { onInsert(insertion) }, size = ControlSize.Compact)
}

@Composable
private fun ToolbarSeparator() {
    val spacing = OneConfigTheme.current.spacing
    Divider(Modifier.padding(horizontal = spacing.xxs, vertical = spacing.s).height(16.dp), vertical = true)
}

@Composable
private fun EditorFooter(note: Note, pinned: Boolean, onClose: () -> Unit) {
    val spacing = OneConfigTheme.current.spacing
    Row(
        Modifier.fillMaxWidth().padding(horizontal = spacing.l, vertical = spacing.l),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.s),
    ) {
        TextButton(
            if (pinned) "Unpin" else "Pin to HUD",
            "hud",
            { NoteActions.setPinned(note, !pinned) },
            style = if (pinned) ControlStyle.Secondary else ControlStyle.Primary,
        )
        IconButton("copy", "Copy note", { NoteActions.copy(note) }, tooltipPlacement = TooltipPlacement.AboveCenter)
        Spacer(Modifier.weight(1f))
        key(note.uuid) {
            ArmedConfirmButton(
                "Delete",
                "trash",
                {
                    NoteActions.delete(note)
                    onClose()
                },
                armedText = "Confirm delete",
            )
        }
    }
}

private fun plural(count: Int, word: String) = if (count == 1) "1 $word" else "$count ${word}s"
