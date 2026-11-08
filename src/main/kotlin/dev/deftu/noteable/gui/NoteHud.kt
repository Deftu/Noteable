package dev.deftu.noteable.gui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import dev.deftu.noteable.Noteable
import dev.deftu.noteable.note.Note
import dev.deftu.noteable.note.NoteBlock
import dev.deftu.noteable.note.NoteBlocks
import dev.deftu.noteable.note.NoteLibrary
import org.polyfrost.compose.composables.PolyBox
import org.polyfrost.compose.composables.PolyCanvas
import org.polyfrost.compose.composables.PolyColumn
import org.polyfrost.compose.composables.PolyMcText
import org.polyfrost.compose.composables.PolyModifier
import org.polyfrost.compose.composables.PolyRow
import org.polyfrost.compose.composables.PolySpacer
import org.polyfrost.compose.composables.PolyText
import org.polyfrost.compose.composables.align
import org.polyfrost.compose.composables.padding
import org.polyfrost.compose.composables.size
import org.polyfrost.compose.layout.PolyAlign
import org.polyfrost.compose.layout.PolyInsets
import org.polyfrost.compose.render.FilterQuality
import org.polyfrost.compose.render.PolyBitmap
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.compose.render.PolyFont
import org.polyfrost.compose.mc.McFontQueue
import org.polyfrost.oneconfig.api.config.v1.Properties.ktProperty
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.Visualizer
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.hud.v1.Font
import org.polyfrost.oneconfig.api.hud.v1.Hud
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.api.notifications.v1.Notifications
import org.polyfrost.oneconfig.api.config.v1.Property
import java.util.UUID
import java.util.function.Predicate

private const val POPPINS_SIZE = 8f
private const val ICON_GAP = 4f
private const val LINE_GAP = 1f
private const val SECTION_GAP = 3f
private const val MARKER_GAP = 3f
private const val BULLET_WIDTH = 5f
private const val BULLET_DOT = 2f
private const val CHECK_BOX = 7f
private const val RULE_SPACE = 3f

/**
 * One pinned note on the HUD
 *
 * The note it shows is part of its saved state, so duplicating the HUD keeps showing the same note until another is picked.
 * It draws its own text so the note's icon can sit beside it, but every style it uses is the HUD's own OneConfig setting.
 */
class NoteHud : Hud("note.json", "Note", Category.INFO) {
    override val description: String = "Shows one of your notes. Pin a note from the Notes page, or duplicate this HUD and pick another note."

    override val searchTags: List<String> = listOf("notes", "noteable", "sticky", "pinned")

    var noteId: String = ""

    @Switch(title = "Show icon", description = "Shows the note's icon beside its title.")
    var showIcon = true

    @Switch(title = "Show title", description = "Shows the note's title above its content.")
    var showTitle = true

    @Switch(title = "Show content", description = "Shows the note's content.")
    var showContent = true

    @Switch(title = "Title divider", description = "Draws a line between the title and the content.")
    var titleDivider = true

    @Slider(title = "Wrap length", description = "Wraps lines longer than this many characters. 0 turns wrapping off.", min = 0f, max = 120f, step = 1f)
    var wrapLength = 40f

    // The options above are plain fields, so this state is what tells Compose to redraw when one changes
    @Transient
    private var optionsRevision = mutableIntStateOf(0)

    init {
        padLeft = 4f
        padRight = 4f
        padTop = 4f
        padBottom = 4f
    }

    val noteUuid: UUID?
        get() = runCatching { UUID.fromString(noteId) }.getOrNull()

    val note: Note?
        get() = noteUuid?.let(NoteLibrary::get)

    override fun addToSerialized(tree: Tree) {
        tree["noteId"] = ktProperty(::noteId, "Note", "The note this HUD shows.").apply {
            addMetadata("visualizer", Visualizer { prop -> NotePicker(prop) })
            addMetadata("fullWidth", true)
        }
    }

    override fun addCallbacks(tree: Tree) {
        // Config.addCallback looks the option up on this HUD's tree, which make() only assigns after this runs
        for (option in arrayOf("noteId", "showIcon", "showTitle", "showContent", "titleDivider", "wrapLength")) {
            @Suppress("UNCHECKED_CAST")
            (tree.getProp(option) as? Property<Any?>)?.addCallback(Predicate {
                optionsRevision.intValue++
                if (option == "noteId") NoteHuds.notePicked()
                false
            })
        }
    }

    override fun clone(): Hud = (super.clone() as NoteHud).apply {
        optionsRevision = mutableIntStateOf(0)
    }

    override fun update(): Boolean = false

    override fun canMergeBackground(): Boolean = true

    override fun minimumSize(): Pair<Float, Float> = (padLeft + padRight + 2f) to (padTop + padBottom + 2f)

    override fun shouldShow(): Boolean = note != null || !isReal || HudManager.isEditing

    @Composable
    override fun Content() {
        NoteLibrary.revision
        optionsRevision.intValue
        val note = note
        val title: String?
        val blocks: List<NoteBlock>
        when {
            note != null -> {
                title = if (showTitle) note.displayTitle else null
                blocks = if (showContent) NoteBlocks.parse(note.content) else emptyList()
            }
            !isReal -> {
                title = "Sticky note"
                blocks = PREVIEW
            }
            else -> {
                title = if (noteId.isBlank()) "Pick a note in this HUD's settings." else "This note was deleted."
                blocks = emptyList()
            }
        }

        val minecraft = font == Font.Minecraft
        val scale = textScale
        val poppins = PolyFont(getPoppinsFontName(), POPPINS_SIZE * scale)
        val lineHeight = if (minecraft) McFontQueue.measureTextHeight(scale) else poppins.metrics.let { it.descent - it.ascent }
        val color = PolyColor(textColor, textChroma, textChromaSpeed)
        val faded = color.withAlpha(0.55f)
        val rule = color.withAlpha(0.3f)
        val static = staticWidth && staticW > 0f && staticH > 0f
        val outer = hudBackground().let { if (static) it.size(staticW, staticH) else it }.padding(PolyInsets(padLeft, padTop, padRight, padBottom))
        val icon = if (showIcon && title != null && (note != null || !isReal)) iconBitmap(note?.icon.orEmpty(), minecraft) else null
        val filter = if (minecraft) FilterQuality.None else FilterQuality.Low
        val uuid = note?.uuid ?: PREVIEW_ID

        // Dividers get an exact width from the measured text: a fill-width child is left out of its column's first
        // measuring pass, so the column (and the background drawn from it) would come up short until the next layout
        fun widest(text: String) = prepare(text).maxOfOrNull { line ->
            if (minecraft) McFontQueue.measureTextWidth(styled(line, false), scale) else poppins.measureTextWidth(line)
        } ?: 0f
        val marker = MARKER_GAP * scale
        val ruleWidth = maxOf(
            title?.let { widest(it) + if (icon != null) lineHeight + ICON_GAP * scale else 0f } ?: 0f,
            blocks.maxOfOrNull { block ->
                when (block) {
                    is NoteBlock.Text -> widest(block.text)
                    is NoteBlock.Bullet -> BULLET_WIDTH * scale + marker + widest(block.text)
                    is NoteBlock.Check -> lineHeight + marker + widest(block.text)
                    else -> 0f
                }
            } ?: 0f,
        )

        PolyBox(outer) {
            PolyColumn(SECTION_GAP * scale, if (static) PolyModifier.align(alignment) else PolyModifier) {
                if (title != null) {
                    PolyRow(ICON_GAP * scale) {
                        if (icon != null) {
                            PolyCanvas(PolyModifier.size(lineHeight, lineHeight)) { x, y, w, h ->
                                image(icon, x, y, w, h, 0f, 0f, 1f, 1f, color, filter)
                            }
                        }
                        Lines(title, color, minecraft)
                    }
                }
                if (title != null && blocks.isNotEmpty() && titleDivider) Rule(rule, scale, ruleWidth)
                if (blocks.isNotEmpty()) {
                    PolyColumn(LINE_GAP * scale) {
                        for (block in blocks) {
                            when (block) {
                                is NoteBlock.Text -> Lines(block.text, color, minecraft)
                                is NoteBlock.Blank -> PolySpacer(height = lineHeight / 2f)
                                is NoteBlock.Divider -> Rule(rule, scale, ruleWidth)
                                is NoteBlock.Bullet -> PolyRow(MARKER_GAP * scale) {
                                    PolyCanvas(PolyModifier.size(BULLET_WIDTH * scale, lineHeight)) { x, y, w, h ->
                                        val dot = BULLET_DOT * scale
                                        rect(x + (w - dot) / 2f, y + (h - dot) / 2f, dot, dot, color, if (minecraft) 0f else dot / 2f)
                                    }
                                    Lines(block.text, color, minecraft)
                                }
                                is NoteBlock.Check -> PolyRow(MARKER_GAP * scale) {
                                    PolyCanvas(PolyModifier.size(lineHeight, lineHeight), live = true) { x, y, w, h ->
                                        val box = CHECK_BOX * scale
                                        val bx = x + (w - box) / 2f
                                        val by = y + (h - box) / 2f
                                        val hovered = isReal && NoteHudInput.track(this, this@NoteHud, uuid,block.line, bx, by, box, box)
                                        val radius = if (minecraft) 0f else scale
                                        if (hovered) rect(bx, by, box, box, color.withAlpha(0.2f), radius)
                                        rectStroke(bx, by, box, box, if (block.checked) faded else color, scale.coerceAtLeast(1f), radius)
                                        if (block.checked) iconBitmap("tick", minecraft)?.let { tick ->
                                            image(tick, bx, by, box, box, 0f, 0f, 1f, 1f, color, filter)
                                        }
                                    }
                                    Lines(block.text, if (block.checked) faded else color, minecraft, strike = block.checked)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Rule(color: PolyColor, scale: Float, width: Float) {
        PolyCanvas(PolyModifier.size(width, RULE_SPACE * scale)) { x, y, w, h ->
            val thickness = scale.coerceAtLeast(1f)
            rect(x, y + (h - thickness) / 2f, w, thickness, color)
        }
    }

    @Composable
    private fun Lines(text: String, color: PolyColor, minecraft: Boolean, strike: Boolean = false) {
        val shadow = if (showShadow) PolyColor(shadowColor, shadowChroma, shadowChromaSpeed) else null
        PolyColumn(LINE_GAP * textScale) {
            for (line in prepare(text)) {
                if (minecraft) {
                    PolyMcText(
                        text = styled(line, strike),
                        outlineColor = if (shadow != null && fullShadow) shadow else null,
                        color = color,
                        shadow = shadow != null && !fullShadow,
                        scale = textScale,
                    )
                } else {
                    PolyText(
                        text = line,
                        color = color,
                        fontSize = POPPINS_SIZE * textScale,
                        shadow = shadow != null,
                        shadowColor = shadow ?: PolyColor.BLACK,
                        shadowOffset = shadowOffsetX.coerceAtLeast(shadowOffsetY).coerceAtLeast(1f),
                        font = getPoppinsFontName(),
                    )
                }
            }
        }
    }

    private fun prepare(text: String): List<String> {
        val cased = when (caseType) {
            1 -> text.uppercase()
            2 -> text.lowercase()
            else -> text
        }
        return wrap(cased, wrapLength.toInt()).split('\n')
    }

    private fun styled(line: String, strike: Boolean): String {
        val head = buildString {
            if (textBold) append("§l")
            if (textItalic) append("§o")
            if (textUnderline) append("§n")
            if (strike) append("§m")
        }
        return if (head.isEmpty()) line else "$head$line§r"
    }

    private companion object {
        private val icons = HashMap<String, PolyBitmap?>()

        val PREVIEW_ID: UUID = UUID(0L, 0L)
        val PREVIEW = NoteBlocks.parse("Pin a note to show it here.\n- Bullets\n[x] Checklists\n---\nAnd dividers")

        /** Minecraft-font HUDs use OneConfig's pixel icon when one exists, like the Minecraft themes do */
        fun iconBitmap(name: String, minecraft: Boolean): PolyBitmap? = synchronized(icons) {
            val paths = when {
                name.isEmpty() -> listOf(Noteable.ICON)
                minecraft -> listOf("/assets/oneconfig/ico/minecraft/$name.svg", "/assets/oneconfig/ico/$name.svg")
                else -> listOf("/assets/oneconfig/ico/$name.svg")
            }
            paths.firstNotNullOfOrNull { path -> icons.getOrPut(path) { Notifications.icon(path, 48) } }
        }

        fun wrap(text: String, length: Int): String {
            if (length <= 0) return text
            return text.split('\n').joinToString("\n") { line ->
                if (line.length <= length) return@joinToString line
                val out = ArrayList<String>()
                var current = ""
                for (word in line.split(' ')) {
                    var rest = word
                    while (rest.length > length) {
                        if (current.isNotEmpty()) out.add(current).also { current = "" }
                        out.add(rest.take(length))
                        rest = rest.drop(length)
                    }
                    current = when {
                        current.isEmpty() -> rest
                        current.length + 1 + rest.length <= length -> "$current $rest"
                        else -> { out.add(current); rest }
                    }
                }
                out.add(current)
                out.joinToString("\n")
            }
        }
    }
}
