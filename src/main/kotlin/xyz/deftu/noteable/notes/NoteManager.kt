package xyz.deftu.noteable.notes

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.client.MinecraftClient
import xyz.deftu.lib.client.gui.DeftuPalette
import xyz.deftu.noteable.Noteable
import xyz.deftu.noteable.NoteableConfig
import xyz.deftu.noteable.events.NoteActionEvent
import xyz.deftu.noteable.exceptions.InvalidConfigException
import xyz.deftu.noteable.gui.NoteComponent
import java.io.File
import java.util.*

object NoteManager {
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    private val notes = mutableListOf<Note>()
    private lateinit var file: File

    fun initialize() {
        file = File(NoteableConfig.configDir, Noteable.NOTES_CONFIG)
        if (!file.exists()) {
            file.createNewFile()
            file.writeText(gson.toJson(JsonArray()))
        }

        // Migrate old note configs so that it can be stored in the new directory
        migrateOld()

        // Perform actions when the game starts
        ClientLifecycleEvents.CLIENT_STARTED.register {
            // Add event listeners
            NoteActionEvent.EVENT.register { action, note ->
                println("NoteActionEvent: $action $note - load")
                when (action) {
                    NoteActionEvent.NoteAction.CREATE -> {
                        Noteable.hudWindow.add(note.createComponent(), Noteable.ID)
                    }
                    NoteActionEvent.NoteAction.REMOVE -> {
                        note.component?.let { Noteable.hudWindow.remove(it) }
                    }
                }
            }

            // Load notes
            load()
        }
    }

    private fun migrateOld() {
        val oldFile = File(File(MinecraftClient.getInstance().runDirectory, "config"), "notes.json")
        if (oldFile.exists()) {
            val element = JsonParser.parseString(oldFile.readText()).asJsonArray
            if (!element.isJsonArray)
                throw InvalidConfigException("OLD notes config should be an array!")

            val json = element.asJsonArray
            json.forEach { element ->
                if (!element.isJsonObject)
                    throw InvalidConfigException("Each note in the notes config should be an object!")

                addNote(NoteParser.parse(element.toString()))
            }

            save()
            oldFile.delete()
        }
    }

    fun addNote(note: Note) {
        if (notes.any {
                it.uuid == note.uuid
        }) {
            addNote(note.copy(uuid = UUID.randomUUID()))
            return
        }

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

            addNote(NoteParser.parse(element.toString()))
        }
    }

    fun getNotes() = notes.toList()

    private fun Note.createComponent() = NoteComponent(this).constrain {
        x = this@createComponent.x.percent
        y = this@createComponent.y.percent
    }.apply {
        note.component = this
    } effect OutlineEffect(DeftuPalette.getPrimary(), 1f)
}
