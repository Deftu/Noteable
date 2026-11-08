package dev.deftu.noteable.config

import dev.deftu.noteable.NoteActions
import dev.deftu.noteable.Noteable
import dev.deftu.noteable.gui.PinnedNotesList
import org.polyfrost.oneconfig.api.config.v1.KtConfig
import org.polyfrost.oneconfig.api.config.v1.Visualizer

object NoteableConfig : KtConfig("noteable.json", Noteable.NAME, Category.QOL, Noteable.ICON) {
    private const val GENERAL = "General"

    val openNotes by button(
        name = "Notes",
        description = "Write, edit, pin and delete your notes.",
        category = GENERAL,
        subcategory = "Notes",
        text = "Open",
    ) { NoteActions.openNotes() }

    val openFolder by button(
        name = "Notes folder",
        description = "Each note is stored as its own file in this folder.",
        category = GENERAL,
        subcategory = "Notes",
        text = "Open",
    ) { NoteActions.openFolder() }

    var pinNewNotes by switch(
        name = "Pin new notes",
        def = false,
        description = "Pins each new note to your HUD as soon as it is created.",
        category = GENERAL,
        subcategory = "Notes",
    )

    val pinnedNotes by DummyProvider(
        "Pinned notes",
        "Each pinned note is its own HUD. Move, style and duplicate them in the HUD editor.",
        GENERAL,
        "HUD",
        Visualizer { PinnedNotesList() },
    ) { addMetadata("fullWidth", true) }
}
