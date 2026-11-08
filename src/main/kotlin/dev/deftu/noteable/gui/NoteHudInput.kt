package dev.deftu.noteable.gui

import dev.deftu.noteable.note.NoteLibrary
import org.polyfrost.compose.render.RenderContext
import org.polyfrost.oneconfig.api.event.v1.EventManager
import org.polyfrost.oneconfig.api.event.v1.events.MouseInputEvent
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.api.platform.v1.Platform
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
//? if <1.9 {
/*import net.minecraft.client.gui.screen.game.ChatScreen
*///?} else {
import net.minecraft.client.gui.screens.ChatScreen
//?}

/**
 * Lets checklist items on a Note HUD be ticked with the mouse while chat is open
 *
 * Each checkbox records where it was last drawn in device pixels; a click inside one of those boxes toggles that line.
 * Only chat counts: other screens draw over the HUD, and mouse events can't be consumed, so a click would reach both.
 */
object NoteHudInput {
    // A box that hasn't been drawn this recently belongs to a HUD that is hidden, moved or gone
    private const val STALE_MS = 250L

    private data class Key(val hud: Int, val note: UUID, val line: Int)

    private class Hit(val note: UUID, val line: Int, val left: Float, val top: Float, val right: Float, val bottom: Float, val drawnAt: Long)

    private val hits = ConcurrentHashMap<Key, Hit>()

    @Volatile
    private var mouseX = -1f

    @Volatile
    private var mouseY = -1f

    fun register() {
        EventManager.register(MouseInputEvent.Moved::class.java, Consumer { e ->
            mouseX = e.x
            mouseY = e.y
        })
        val left = Platform.compatibility().keys().mouseButtonLeft
        EventManager.register(MouseInputEvent::class.java, Consumer { e ->
            if (e.button == left && e.state == MouseInputEvent.PRESSED) click()
        })
    }

    private fun interactive(): Boolean = !HudManager.isEditing && Platform.screen().current<Any?>() is ChatScreen

    /** Records a checkbox of [hud] drawn at ([x], [y]) in the canvas' own space, and returns whether the mouse is over it */
    fun track(ctx: RenderContext, hud: Any, note: UUID, line: Int, x: Float, y: Float, w: Float, h: Float): Boolean {
        val transform = ctx.deviceTransform() ?: return false
        val sx = transform[0]
        val sy = transform[1]
        if (sx == 0f || sy == 0f) return false
        val hit = Hit(note, line, x * sx + transform[2], y * sy + transform[3], (x + w) * sx + transform[2], (y + h) * sy + transform[3], System.currentTimeMillis())
        hits[Key(System.identityHashCode(hud), note, line)] = hit
        return interactive() && contains(hit)
    }

    private fun contains(hit: Hit): Boolean {
        if (mouseX < 0f) return false
        val ratio = Platform.screen().pixelRatio()
        val px = mouseX * ratio
        val py = mouseY * ratio
        return px in hit.left..hit.right && py in hit.top..hit.bottom
    }

    private fun click() {
        if (!interactive()) return
        val now = System.currentTimeMillis()
        hits.values.removeIf { now - it.drawnAt > STALE_MS }
        // The most recently drawn box is the one on top
        val hit = hits.values.filter(::contains).maxByOrNull { it.drawnAt } ?: return
        NoteLibrary.toggleCheck(hit.note, hit.line)
    }
}
