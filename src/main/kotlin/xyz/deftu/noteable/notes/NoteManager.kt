package xyz.deftu.noteable.notes

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.client.MinecraftClient
import xyz.deftu.noteable.Noteable
import xyz.deftu.noteable.events.NoteActionEvent
import xyz.deftu.noteable.exceptions.InvalidConfigException
import java.io.File

object NoteManager {
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    private val notes = mutableListOf<Note>()
    private lateinit var file: File

    fun initialize() {
        file = File(File(MinecraftClient.getInstance().runDirectory, "config"), Noteable.NOTES_CONFIG)
        if (!file.exists()) {
            file.createNewFile()
            file.writeText(gson.toJson(JsonArray()))
        }

        ClientLifecycleEvents.CLIENT_STARTED.register {
            load()
        }
    }

    fun addNote(note: Note) {
        notes.add(note)
        NoteActionEvent.EVENT.invoker().onNoteAction(NoteActionEvent.NoteAction.CREATE, note)
    }

    fun removeNote(note: Note) {
        notes.remove(note)
        NoteActionEvent.EVENT.invoker().onNoteAction(NoteActionEvent.NoteAction.REMOVE, note)
    }

    fun save() {
        val array = JsonArray()
        notes.map(NoteParser::serialize).forEach(array::add)
        file.writeText(gson.toJson(array))
    }

    fun load() {
        val element = JsonParser.parseString(file.readText())
        if (!element.isJsonArray)
            throw InvalidConfigException("Notes config should be an array!")

        val array = element.asJsonArray
        array.forEach { element ->
            if (!element.isJsonObject)
                throw InvalidConfigException("Each note in the notes config should be an object!")

            notes.add(NoteParser.parse(element.toString()))
        }
    }

    fun getNotes() = notes.toList()
}
