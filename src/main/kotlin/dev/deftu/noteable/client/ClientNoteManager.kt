package dev.deftu.noteable.client

import com.google.gson.GsonBuilder
import dev.deftu.noteable.NoteableConstants
import dev.deftu.noteable.client.gui.HudRenderer
import dev.deftu.noteable.note.Note
import dev.deftu.noteable.note.NoteManager
import dev.deftu.omnicore.common.OmniLoader
import java.io.File
import java.util.UUID

object ClientNoteManager : NoteManager {

    private var isInitialized = false
    private val notes = mutableListOf<Note>()

    private val gson by lazy {
        GsonBuilder()
            .setPrettyPrinting()
            .create()
    }

    private val directory by lazy {
        val directory = File(OmniLoader.configDir.toFile(), NoteableConstants.NAME)
        if (!directory.exists() && !directory.mkdirs()) {
            throw IllegalStateException("Could not create Noteable config directory!")
        }

        directory
    }

    fun initialize() {
        if (isInitialized) {
            return
        }

        refresh()

        isInitialized = true
    }

    fun refresh() {
        directory.listFiles { file ->
            file.isFile && file.exists() && try {
                UUID.fromString(file.nameWithoutExtension)
                true
            } catch (t: Throwable) {
                false
            }
        }?.mapNotNull(::read)?.forEach(notes::add)
    }

    fun updateAll() {
        notes.forEach(::update)
    }

    override fun add(note: Note) {
        notes.add(note)
        write(note)
        HudRenderer.add(note)
    }

    override fun remove(note: Note) {
        notes.remove(note)
        delete(note)
        HudRenderer.remove(note)
    }

    override fun update(note: Note) {
        val index = notes.indexOfFirst { it.uuid == note.uuid }
        if (index != -1) {
            notes[index] = note
        }

        write(note)
        HudRenderer.update(note)
    }

    override fun get(): List<Note> {
        return notes.toList()
    }

    private fun read(uuid: UUID): Note? {
        val file = File(directory, "$uuid.json")
        return read(file)
    }

    private fun read(file: File): Note? {
        if (!file.exists()) {
            return null
        }

        return gson.fromJson(file.readText(), Note::class.java)
    }

    private fun write(note: Note) {
        val file = File(directory, "${note.uuid}.json")
        file.writeText(gson.toJson(note))
    }

    private fun delete(note: Note) {
        val file = File(directory, "${note.uuid}.json")
        if (file.exists()) {
            file.delete()
        }
    }

}
