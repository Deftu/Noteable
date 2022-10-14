package xyz.deftu.noteable.notes

import java.util.UUID

data class Note(
    val uuid: UUID,
    val title: String,
    val content: String,
    var sticky: Boolean,
    var x: Int,
    var y: Int
)
