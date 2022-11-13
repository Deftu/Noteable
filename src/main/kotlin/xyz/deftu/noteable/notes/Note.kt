package xyz.deftu.noteable.notes

import xyz.deftu.noteable.gui.NoteComponent
import java.util.UUID

data class Note(
    val uuid: UUID,
    val title: String,
    val content: String,
    var sticky: Boolean,
    var x: Float,
    var y: Float
) {
    internal var component: NoteComponent? = null
}
