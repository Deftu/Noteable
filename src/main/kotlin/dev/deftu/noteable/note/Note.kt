package dev.deftu.noteable.note

import java.util.UUID

data class Note(
    val uuid: UUID,
    val title: String,
    val content: String,
    /** Only set on notes pinned before each pin became its own HUD, cleared once that HUD is made */
    val isSticky: Boolean,
    /** An OneConfig icon name, or empty for the default note icon */
    val icon: String,
    val createdAt: Long,
    val updatedAt: Long,
) {
    val displayTitle: String
        get() = title.ifBlank { "Untitled note" }
}
