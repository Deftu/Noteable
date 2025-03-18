package dev.deftu.noteable.client.migration

//#if MC >= 1.18.2 && MC <= 1.19.2
import com.google.gson.GsonBuilder
import dev.deftu.noteable.client.ClientNoteManager
import dev.deftu.noteable.note.Note
import dev.deftu.omnicore.common.OmniLoader
import java.io.File

object V1_0_Migrator {

    private const val NAME = "notes.json"
    private const val NEW_NAME = "1.0-migrated-notes.json"

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    private lateinit var file: File

    fun migrate() {
        file = File(OmniLoader.configDir.toFile(), NAME)
        if (!file.exists()) {
            return
        }

        // Move all the old notes to the new system
        val notes = gson.fromJson(file.readText(), Array<Note>::class.java)
        notes.forEach(ClientNoteManager::add)

        // Move the old file to a new file
        file.renameTo(File(LegacyMigrations.migrationsDir, NEW_NAME))
    }

}
//#endif
