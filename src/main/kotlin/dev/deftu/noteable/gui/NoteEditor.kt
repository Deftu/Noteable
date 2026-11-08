@file:OptIn(ExperimentalOneConfigUiApi::class, ExperimentalLayoutApi::class)

package dev.deftu.noteable.gui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.deftu.noteable.NoteActions
import dev.deftu.noteable.Noteable
import dev.deftu.noteable.PlayerContext
import dev.deftu.noteable.note.Note
import dev.deftu.noteable.note.NoteLibrary
import kotlinx.coroutines.delay
import org.polyfrost.oneconfig.api.ui.v1.ExperimentalOneConfigUiApi
import org.polyfrost.oneconfig.api.ui.v1.components.Icon
import org.polyfrost.oneconfig.api.ui.v1.components.IconButton
import org.polyfrost.oneconfig.api.ui.v1.components.Text
import org.polyfrost.oneconfig.api.ui.v1.components.Tooltip
import org.polyfrost.oneconfig.api.ui.v1.components.onClick
import org.polyfrost.oneconfig.api.ui.v1.components.rememberInteractionSource
import org.polyfrost.oneconfig.api.ui.v1.theme.OneConfigTheme

internal val EDITOR_WIDTH = 360.dp

/** OneConfig's own glyphs, so each one has a Minecraft-theme counterpart; the first is Noteable's default */
internal val NOTE_ICONS = listOf(
    Noteable.ICON, "star", "bell", "info", "alert-circle", "check-circle", "lock", "combat",
    "spanner", "scissors", "paintbrush", "layers", "cloud", "moon", "sun", "keyboard", "console", "hypixel",
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
    val theme = OneConfigTheme.current
    return width(EDITOR_WIDTH)
        .fillMaxHeight()
        .padding(bottom = 24.dp)
        .background(theme.modCardBackground, theme.modCardShape)
        .border(1.dp, theme.borderColor, theme.modCardShape)
}

@Composable
internal fun EditorPane(note: Note, pinned: Boolean, onClose: () -> Unit) {
    val theme = OneConfigTheme.current
    var title by remember(note.uuid) { mutableStateOf(note.title) }
    var content by remember(note.uuid) { mutableStateOf(TextFieldValue(note.content, TextRange(note.content.length))) }
    var pickingIcon by remember(note.uuid) { mutableStateOf(false) }

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

    Column(Modifier.editorFrame()) {
        Column(Modifier.padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tooltip({ TooltipText("Change icon") }, Modifier, Alignment.BottomCenter) {
                    IconOnlyButton(note.icon.ifEmpty { Noteable.ICON }, selected = pickingIcon) { pickingIcon = !pickingIcon }
                }
                TitleField(title, Modifier.weight(1f)) {
                    title = it
                    NoteLibrary.edit(note.uuid, title = it)
                }
                IconButton("close", Modifier.size(28.dp)) { onClose() }
            }
            Row(Modifier.padding(start = 38.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Edited ${editedLabel(note.updatedAt)}", Modifier, theme.textColorSecondary, 11.sp)
                if (pinned) StatusTag("Pinned")
            }
        }

        if (pickingIcon) {
            FlowRow(
                Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                for (icon in NOTE_ICONS) {
                    val value = if (icon == Noteable.ICON) "" else icon
                    IconOnlyButton(icon, selected = note.icon == value) {
                        NoteLibrary.setIcon(note.uuid, value)
                        pickingIcon = false
                    }
                }
            }
        }

        Divider()

        key(note.uuid) {
            ContentEditor(
                content,
                Modifier.weight(1f).fillMaxWidth().padding(14.dp),
                onChange = ::setContent,
                onInsert = ::insert,
            )
        }

        Divider()

        EditorFooter(note, pinned, onClose)
    }
}

@Composable
private fun TitleField(value: String, modifier: Modifier, onChange: (String) -> Unit) {
    val theme = OneConfigTheme.current
    val interaction = rememberInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    val focused by interaction.collectIsFocusedAsState()
    val background by animateColorAsState(if (hovered || focused) theme.componentBackground else Color.Transparent)
    Box(
        modifier
            .height(30.dp)
            .background(background, theme.buttonShape)
            .hoverable(interaction)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) Text("Untitled note", Modifier, theme.textColorSecondary, 16.sp, FontWeight.Medium)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(color = theme.textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium, fontFamily = theme.fontFamily),
            cursorBrush = SolidColor(OneConfigTheme.accent),
            interactionSource = interaction,
            modifier = Modifier.fillMaxWidth(),
        )
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
    val interaction = rememberInteractionSource()
    val focused by interaction.collectIsFocusedAsState()
    val hovered by interaction.collectIsHoveredAsState()
    val border by animateColorAsState(
        when {
            focused -> OneConfigTheme.accent
            hovered -> theme.textColorSecondary.copy(alpha = 0.6f)
            else -> theme.borderColor
        },
    )

    Column(
        modifier
            .background(theme.componentBackground, theme.buttonShape)
            .border(1.dp, border, theme.buttonShape)
            .hoverable(interaction),
    ) {
        FlowRow(
            Modifier.fillMaxWidth().padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            ToolbarButton(LOCATION, onInsert)
            ToolbarSeparator()
            for (insertion in CONTEXT_INSERTIONS) ToolbarButton(insertion, onInsert)
            ToolbarSeparator()
            for (insertion in FORMAT_INSERTIONS) ToolbarButton(insertion, onInsert)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(theme.borderColor))
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            // The field fills the visible area so clicking below the text still focuses it
            val minHeight = (maxHeight - 20.dp).coerceAtLeast(0.dp)
            Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 10.dp)) {
                if (value.text.isEmpty()) Text("Write something, or insert where you are from the toolbar.", Modifier, theme.textColorSecondary, 12.sp)
                BasicTextField(
                    value = value,
                    onValueChange = onChange,
                    textStyle = TextStyle(color = theme.textColor, fontSize = 12.sp, fontFamily = theme.fontFamily, lineHeight = 18.sp),
                    cursorBrush = SolidColor(OneConfigTheme.accent),
                    interactionSource = interaction,
                    modifier = Modifier.fillMaxWidth().heightIn(min = minHeight),
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
            Spacer(Modifier.weight(1f))
            val lines = if (value.text.isEmpty()) 0 else value.text.count { it == '\n' } + 1
            Text("${plural(lines, "line")} · ${plural(value.text.length, "character")}", Modifier, theme.textColorSecondary, 11.sp)
        }
    }
}

@Composable
private fun ToolbarButton(insertion: Insertion, onInsert: (Insertion) -> Unit) {
    val theme = OneConfigTheme.current
    val interaction = rememberInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    val background by animateColorAsState(if (hovered) theme.chipBackground else Color.Transparent)
    Tooltip({ TooltipText(insertion.label) }, Modifier, Alignment.BottomCenter) {
        Box(
            Modifier
                .size(26.dp)
                .background(background, theme.buttonShape)
                .hoverable(interaction)
                .pointerHoverIcon(PointerIcon.Hand)
                .onClick(interaction) { onInsert(insertion) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(insertion.icon, if (hovered) theme.textColor else theme.textColor.copy(alpha = 0.7f), Modifier.size(15.dp))
        }
    }
}

@Composable
private fun ToolbarSeparator() {
    Box(Modifier.padding(horizontal = 3.dp, vertical = 5.dp).width(1.dp).height(16.dp).background(OneConfigTheme.current.borderColor))
}

@Composable
private fun EditorFooter(note: Note, pinned: Boolean, onClose: () -> Unit) {
    var confirmDelete by remember(note.uuid) { mutableStateOf(false) }
    LaunchedEffect(confirmDelete) {
        if (confirmDelete) {
            delay(3000)
            confirmDelete = false
        }
    }

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SmallButton(
            if (pinned) "Unpin" else "Pin to HUD",
            "hud",
            hierarchy = if (pinned) ButtonHierarchy.SECONDARY else ButtonHierarchy.PRIMARY,
        ) { NoteActions.setPinned(note, !pinned) }
        Tooltip({ TooltipText("Copy note") }, Modifier, Alignment.TopCenter) {
            IconOnlyButton("copy") { NoteActions.copy(note) }
        }
        Spacer(Modifier.weight(1f))
        if (confirmDelete) {
            SmallButton("Confirm delete", "trash", hierarchy = ButtonHierarchy.PRIMARY, destructive = true) {
                NoteActions.delete(note)
                onClose()
            }
        } else {
            SmallButton("Delete", "trash", destructive = true) { confirmDelete = true }
        }
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(OneConfigTheme.current.borderColor))
}

@Composable
internal fun TooltipText(text: String) {
    Text(text, Modifier, OneConfigTheme.current.textColor, 12.sp)
}

private fun plural(count: Int, word: String) = if (count == 1) "1 $word" else "$count ${word}s"
