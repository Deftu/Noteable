package dev.deftu.noteable.note

interface NoteManager {

    fun add(note: Note)

    fun remove(note: Note)

    fun update(note: Note)

    fun get(): List<Note>

}
