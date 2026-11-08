package dev.deftu.noteable.note

/** A note's content split into the pieces the HUD draws differently */
sealed class NoteBlock {
    data class Text(val text: String) : NoteBlock()
    data class Bullet(val text: String) : NoteBlock()

    /** [line] is the checklist's line in the note's content, used to toggle it */
    data class Check(val text: String, val checked: Boolean, val line: Int) : NoteBlock()
    object Divider : NoteBlock()
    object Blank : NoteBlock()
}

object NoteBlocks {
    private val DIVIDER = Regex("""^\s*(-{3,}|_{3,}|={3,})\s*$""")
    private val CHECK = Regex("""^(\s*)\[([ xX])]\s?(.*)$""")
    private val BULLET = Regex("""^\s*[-*•]\s+(.*)$""")

    fun parse(content: String): List<NoteBlock> {
        val lines = content.split('\n')
        val last = lines.indexOfLast { it.isNotBlank() }
        return lines.take(last + 1).mapIndexed { index, line ->
            DIVIDER.matchEntire(line)?.let { return@mapIndexed NoteBlock.Divider }
            CHECK.matchEntire(line)?.let { return@mapIndexed NoteBlock.Check(it.groupValues[3], it.groupValues[2] != " ", index) }
            BULLET.matchEntire(line)?.let { return@mapIndexed NoteBlock.Bullet(it.groupValues[1]) }
            if (line.isBlank()) NoteBlock.Blank else NoteBlock.Text(line)
        }
    }

    /** Flips the checklist item on [line], or returns null when that line isn't one */
    fun toggle(content: String, line: Int): String? {
        val lines = content.split('\n').toMutableList()
        val match = lines.getOrNull(line)?.let(CHECK::matchEntire) ?: return null
        val (indent, mark, text) = match.destructured
        lines[line] = "$indent[${if (mark == " ") "x" else " "}] $text"
        return lines.joinToString("\n")
    }
}
