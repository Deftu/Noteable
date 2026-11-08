package dev.deftu.noteable.gui

import dev.deftu.noteable.Noteable
import dev.deftu.noteable.config.NoteableConfig
import dev.deftu.noteable.note.Note
import dev.deftu.noteable.note.NoteLibrary
import org.polyfrost.oneconfig.api.event.v1.EventManager
import org.polyfrost.oneconfig.api.event.v1.events.TickEvent
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import androidx.compose.runtime.mutableIntStateOf
import java.util.UUID
import java.util.function.Consumer
import java.util.concurrent.atomic.AtomicBoolean

/** A note is pinned while at least one [NoteHud] shows it */
object NoteHuds {
    private const val ORIGIN = 10f
    private const val STEP = 24f

    private val migrated = AtomicBoolean(false)

    // Bumped when a HUD is pointed at another note from its settings, which OneConfig doesn't count as an instance change
    private val picked = mutableIntStateOf(0)

    fun register() {
        HudManager.register(NoteHud(), NoteableConfig.id, Noteable.ICON)
        // HUD instances only exist once OneConfig has loaded the profile, which is done by the first tick
        EventManager.register(TickEvent.End::class.java, Consumer { if (migrated.compareAndSet(false, true)) migrateLegacyPins() })
    }

    /** Snapshot state, so composables reading this recompose when HUDs are added or removed */
    val revision: Int
        get() = HudManager.instancesRevision.intValue + NoteLibrary.revision + picked.intValue

    internal fun notePicked() {
        picked.intValue++
    }

    fun instances(): List<NoteHud> = HudManager.getHudsOfType(NoteHud::class.java)

    fun instancesFor(uuid: UUID): List<NoteHud> = instances().filter { it.noteUuid == uuid }

    fun isPinned(note: Note): Boolean = instancesFor(note.uuid).isNotEmpty()

    fun pinnedNotes(): List<Note> = instances().mapNotNull { it.note }.distinctBy { it.uuid }.sortedBy { it.createdAt }

    // Mirrors how OneConfig's own HUD library adds an instance, as it has no public API for doing so
    fun pin(note: Note): NoteHud? {
        instancesFor(note.uuid).firstOrNull()?.let { return it }
        val provider = HudManager.getProvider(NoteHud::class.java) ?: return null
        var added: NoteHud? = null
        return runCatching {
            val hud = provider.make() as NoteHud
            hud.noteId = note.uuid.toString()
            val offset = instances().size * STEP
            hud.setAbsolutePosition(ORIGIN, ORIGIN + offset)
            HudManager.markProviderKnown(hud)
            HudManager.activeInstances.add(hud)
            added = hud
            HudManager.instancesRevision.intValue++
            hud.setup()
            hud.captureStaticSizeDefaults()
            hud.capturePositionDefaults()
            hud.save()
            HudManager.invalidate()
            hud
        }.onFailure {
            Noteable.LOGGER.error("Failed to add a HUD for note {}", note.uuid, it)
            added?.let { hud -> runCatching { HudManager.removeHud(hud, delete = true) } }
            HudManager.invalidate()
        }.getOrNull()
    }

    fun unpin(note: Note) {
        for (hud in instancesFor(note.uuid)) HudManager.removeHud(hud, delete = true)
        HudManager.invalidate()
    }

    private fun migrateLegacyPins() {
        for (note in NoteLibrary.all().filter { it.isSticky }) {
            if (!isPinned(note) && pin(note) == null) continue
            NoteLibrary.clearLegacyPin(note.uuid)
        }
    }
}
