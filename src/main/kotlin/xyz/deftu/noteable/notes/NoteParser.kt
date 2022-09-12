package xyz.deftu.noteable.notes

import com.google.gson.JsonObject
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import xyz.deftu.noteable.exceptions.InvalidConfigException
import java.io.StringReader
import java.io.StringWriter
import java.util.UUID

object NoteParser {
    fun parse(input: String): Note {
        var note: Note? = null

        JsonReader(StringReader(input)).use { reader ->
            val token = reader.peek()
            if (token != JsonToken.BEGIN_OBJECT)
                throw InvalidConfigException("Each note in the config should be an object!")

            var noteUuid: UUID? = null
            var noteTitle = ""
            var noteContent = ""
            var noteSticky = false
            var noteX = 0
            var noteY = 0

            reader.beginObject()

            var currentName = ""
            while (reader.hasNext()) {
                val token = reader.peek()
                if (token == JsonToken.NAME) {
                    currentName = reader.nextName()
                    continue
                }

                when (currentName) {
                    "uuid" -> {
                        if (token != JsonToken.STRING)
                            throw InvalidConfigException("Note UUID should be a string!")
                        noteUuid = UUID.fromString(reader.nextString())
                    }
                    "title" -> {
                        if (token != JsonToken.STRING)
                            throw InvalidConfigException("Note title should be a string!")
                        noteTitle = reader.nextString()
                    }
                    "content" -> {
                        if (token != JsonToken.STRING)
                            throw InvalidConfigException("Note content should be a string!")
                        noteContent = reader.nextString()
                    }
                    "sticky" -> {
                        if (token != JsonToken.BOOLEAN)
                            throw InvalidConfigException("Note sticky value should be a boolean!")
                        noteSticky = reader.nextBoolean()
                    }
                    "x" -> {
                        if (token != JsonToken.NUMBER)
                            throw InvalidConfigException("Note x position should be an integer!")
                        noteX = reader.nextInt()
                    }
                    "y" -> {
                        if (token != JsonToken.NUMBER)
                            throw InvalidConfigException("Note y position should be an integer!")
                        noteY = reader.nextInt()
                    }
                }
            }

            reader.endObject()

            if (noteUuid == null)
                throw InvalidConfigException("Could not find note UUID!")
            if (noteTitle.isBlank())
                throw InvalidConfigException("Note title cannot be blank!")
            if (noteContent.isBlank())
                throw InvalidConfigException("Note content cannot be blank!")

            note = Note(noteUuid, noteTitle, noteContent, noteSticky, noteX, noteY)
        }

        if (note == null) // This should always be false, but just in case...
            throw InvalidConfigException("Failed to parse note!")

        return note as Note
    }

    fun serialize(note: Note): JsonObject {
        val obj = JsonObject()
        obj.addProperty("uuid", note.uuid.toString())
        obj.addProperty("title", note.title)
        obj.addProperty("content", note.content)
        obj.addProperty("sticky", note.sticky)
        obj.addProperty("x", note.x)
        obj.addProperty("y", note.y)
        return obj
    }
}
