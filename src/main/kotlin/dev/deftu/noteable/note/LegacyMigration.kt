package dev.deftu.noteable.note

import com.google.gson.JsonParser
import dev.deftu.noteable.Noteable
import net.fabricmc.loader.api.FabricLoader
import java.io.File

/** Moves notes from the 1.2 single-file format into one file per note */
object LegacyMigration {
    private const val LEGACY_FILE = "notes.json"
    private const val MIGRATED_FILE = "1.2-migrated-notes.json"

    fun run() {
        val file = FabricLoader.getInstance().configDir.resolve(Noteable.ID).resolve(LEGACY_FILE).toFile()
        if (!file.isFile) return

        val array = runCatching { JsonParser.parseString(file.readText()) }
            .onFailure { Noteable.LOGGER.error("Failed to read {}", LEGACY_FILE, it) }
            .getOrNull()?.takeIf { it.isJsonArray }?.asJsonArray ?: return
        // One bad entry shouldn't hold back the rest, or the migration would fail again on every launch
        val notes = array.mapNotNull { element ->
            runCatching { element.takeIf { it.isJsonObject }?.asJsonObject?.let { NoteLibrary.parse(it, file.lastModified()) } }
                .onFailure { Noteable.LOGGER.warn("Skipped a note in {} that couldn't be read", LEGACY_FILE, it) }
                .getOrNull()
        }
        NoteLibrary.import(notes)

        // The legacy file must not survive, or notes deleted since would come back on the next launch
        val migrations = File(NoteLibrary.directory, "migrations").apply { mkdirs() }
        val target = generateSequence(0) { it + 1 }
            .map { File(migrations, if (it == 0) MIGRATED_FILE else MIGRATED_FILE.replace(".json", "-$it.json")) }
            .first { !it.exists() }
        if (!file.renameTo(target) && !file.delete()) Noteable.LOGGER.error("Couldn't move {} out of the way", LEGACY_FILE)
        Noteable.LOGGER.info("Migrated {} notes from {}", notes.size, LEGACY_FILE)
    }
}
