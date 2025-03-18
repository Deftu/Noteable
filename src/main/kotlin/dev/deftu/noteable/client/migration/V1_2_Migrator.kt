package dev.deftu.noteable.client.migration

//#if MC >= 1.18.2 && MC <= 1.19.2
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import dev.deftu.noteable.NoteableConstants
import dev.deftu.noteable.client.ClientNoteManager
import dev.deftu.noteable.note.Note
import dev.deftu.omnicore.common.OmniLoader
import java.io.File

object V1_2_Migrator {

    private const val NAME = "notes.json"
    private const val NEW_NAME = "1.2-migrated-notes.json"

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    private val directory by lazy {
        val modDir = File(OmniLoader.configDir.toFile(), NoteableConstants.ID)
        if (!modDir.exists() && !modDir.mkdirs())
            throw IllegalStateException("Could not create Noteable config directory!")

        modDir
    }

    private lateinit var file: File

    fun migrate() {
        file = File(directory, NAME)
        if (!file.exists()) {
            return
        }

        // Move all the old notes to the new system
        val notes = load()
        notes.forEach(ClientNoteManager::add)

        // Move the old file to a new file
        file.renameTo(File(LegacyMigrations.migrationsDir, NEW_NAME))
    }

    fun load(): List<Note> {
        val element = JsonParser.parseString(file.readText())
        if (!element.isJsonArray) {
            throw IllegalStateException("Notes config should be an array!")
        }

        val array = element.asJsonArray
        return array.map { element ->
            if (!element.isJsonObject) {
                throw IllegalStateException("Each note in the notes config should be an object!")
            }

            gson.fromJson(element, Note::class.java)
        }
    }

}
//#endif
