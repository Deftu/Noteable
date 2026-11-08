package dev.deftu.noteable.note

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.deftu.noteable.Noteable
import net.fabricmc.loader.api.FabricLoader
import org.polyfrost.oneconfig.utils.v1.Multithreading
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

object NoteLibrary {
    private const val WRITE_DELAY_MS = 400L
    private const val MIN_PREFIX = 4

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val notes = ConcurrentHashMap<UUID, Note>()
    private val dirty = ConcurrentHashMap.newKeySet<UUID>()
    private val flushScheduled = AtomicBoolean(false)
    private val ioLock = Any()

    /** Bumped on every change so Compose readers recompose */
    var revision by mutableIntStateOf(0)
        private set

    val directory: File by lazy {
        FabricLoader.getInstance().configDir.resolve(Noteable.NAME).toFile().apply { mkdirs() }
    }

    fun load() {
        val files = directory.listFiles { file -> file.isFile && file.extension == "json" && parseUuid(file.nameWithoutExtension) != null }.orEmpty()
        for (file in files) {
            val note = runCatching { read(file) }
                .onFailure { Noteable.LOGGER.error("Failed to read note {}", file.name, it) }
                .getOrNull()
            if (note == null) {
                Noteable.LOGGER.warn("Skipped {}, which isn't a valid note", file.name)
                continue
            }
            notes[note.uuid] = note
            // Writes and deletes use the canonical name, so a differently named file would bring the note back after deleting it
            if (file.name != fileFor(note.uuid).name && !file.renameTo(fileFor(note.uuid))) file.delete()
        }
        Runtime.getRuntime().addShutdownHook(Thread(::flush, "${Noteable.NAME} note saver"))
        changed()
    }

    /** Newest edits first */
    fun all(): List<Note> = notes.values.sortedByDescending { it.updatedAt }

    fun get(uuid: UUID): Note? = notes[uuid]

    fun find(query: String): Note? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null
        parseUuid(trimmed)?.let { notes[it] }?.let { return it }
        val all = all()
        all.firstOrNull { it.title.equals(trimmed, ignoreCase = true) }?.let { return it }
        if (trimmed.length < MIN_PREFIX) return null
        return all.filter { it.uuid.toString().startsWith(trimmed, ignoreCase = true) }.singleOrNull()
    }

    fun create(title: String = "", content: String = ""): Note {
        val now = System.currentTimeMillis()
        val note = Note(UUID.randomUUID(), title, normalize(content), false, "", now, now)
        put(note)
        return note
    }

    fun edit(uuid: UUID, title: String? = null, content: String? = null): Note? {
        val note = notes[uuid] ?: return null
        val nextTitle = title ?: note.title
        val nextContent = content?.let(::normalize) ?: note.content
        if (nextTitle == note.title && nextContent == note.content) return note
        return note.copy(title = nextTitle, content = nextContent, updatedAt = System.currentTimeMillis()).also(::put)
    }

    fun toggleCheck(uuid: UUID, line: Int): Note? {
        val note = notes[uuid] ?: return null
        val content = NoteBlocks.toggle(note.content, line) ?: return null
        return edit(uuid, content = content)
    }

    fun setIcon(uuid: UUID, icon: String): Note? {
        val note = notes[uuid] ?: return null
        if (note.icon == icon) return note
        return note.copy(icon = icon, updatedAt = System.currentTimeMillis()).also(::put)
    }

    fun clearLegacyPin(uuid: UUID) {
        val note = notes[uuid] ?: return
        if (note.isSticky) put(note.copy(isSticky = false))
    }

    fun delete(uuid: UUID): Boolean {
        if (notes.remove(uuid) == null) return false
        changed()
        persist(uuid)
        return true
    }

    /** Adds notes from an older format, skipping any that already exist */
    fun import(imported: Collection<Note>) {
        for (note in imported) {
            if (notes.putIfAbsent(note.uuid, note) == null) persist(note.uuid)
        }
        changed()
    }

    fun parse(json: JsonObject, fallbackTime: Long): Note? {
        fun primitive(key: String) = json.get(key)?.takeIf { it.isJsonPrimitive }?.asJsonPrimitive
        val uuid = primitive("uuid")?.asString?.let(::parseUuid) ?: return null
        val created = primitive("createdAt")?.asLong ?: fallbackTime
        return Note(
            uuid = uuid,
            title = primitive("title")?.asString.orEmpty(),
            content = normalize(primitive("content")?.asString.orEmpty()),
            isSticky = primitive("sticky")?.asBoolean ?: false,
            icon = primitive("icon")?.asString.orEmpty(),
            createdAt = created,
            updatedAt = primitive("updatedAt")?.asLong ?: created,
        )
    }

    private fun put(note: Note) {
        notes[note.uuid] = note
        changed()
        persist(note.uuid)
    }

    private fun normalize(content: String) = content.replace("\r\n", "\n").replace('\r', '\n')

    private fun fileFor(uuid: UUID) = File(directory, "$uuid.json")

    private fun changed() {
        revision++
    }

    // Edits arrive once per keystroke, so writes are batched and each flush stores the note as it is at that moment
    private fun persist(uuid: UUID) {
        dirty.add(uuid)
        if (flushScheduled.compareAndSet(false, true)) {
            Multithreading.schedule(::flush, WRITE_DELAY_MS, TimeUnit.MILLISECONDS)
        }
    }

    private fun flush() {
        flushScheduled.set(false)
        synchronized(ioLock) {
            val pending = dirty.toList()
            dirty.removeAll(pending.toSet())
            for (uuid in pending) {
                runCatching { write(uuid) }.onFailure {
                    // Kept dirty so the next save, or the one on shutdown, tries again
                    dirty.add(uuid)
                    Noteable.LOGGER.error("Failed to save note {}", uuid, it)
                }
            }
        }
    }

    private fun write(uuid: UUID) {
        val file = fileFor(uuid).toPath()
        val note = notes[uuid]
        if (note == null) {
            Files.deleteIfExists(file)
            return
        }
        // Written beside the note and moved over it, so a crash mid-write can't leave a truncated note behind
        val temp = file.resolveSibling("${file.fileName}.tmp")
        Files.write(temp, gson.toJson(serialize(note)).toByteArray(Charsets.UTF_8))
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun serialize(note: Note) = JsonObject().apply {
        addProperty("uuid", note.uuid.toString())
        addProperty("title", note.title)
        addProperty("content", note.content)
        addProperty("sticky", note.isSticky)
        addProperty("icon", note.icon)
        addProperty("createdAt", note.createdAt)
        addProperty("updatedAt", note.updatedAt)
    }

    private fun read(file: File): Note? {
        val json = JsonParser.parseString(file.readText()).takeIf { it.isJsonObject }?.asJsonObject ?: return null
        return parse(json, file.lastModified())
    }

    private fun parseUuid(value: String): UUID? = runCatching { UUID.fromString(value) }.getOrNull()
}
