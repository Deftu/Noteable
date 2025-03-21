package dev.deftu.noteable.client.gui

import dev.deftu.eventbus.on
import dev.deftu.noteable.client.ClientNoteManager
import dev.deftu.noteable.client.gui.components.NoteComponent
import dev.deftu.noteable.note.Note
import dev.deftu.omnicore.OmniCore
import dev.deftu.omnicore.client.events.HudRenderEvent
import dev.deftu.omnicore.client.events.OmniClientEventPassthrough
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.UMatrixStack
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture

object HudRenderer {

    private const val UNRENDERED_REFRESH_TIME = 5_000L // The amount of time when the HUD is no longer being rendered before we need to refresh upon it being rendered again
    private var firstRefresh = false
    private val refreshScheduler = Executors.newSingleThreadScheduledExecutor()
    private var scheduledRefresh: ScheduledFuture<*>? = null

    private val window = Window(ElementaVersion.V8)

    private val noteContainer by UIContainer().constrain {
        width = 100.percent
        height = 100.percent
    } childOf window

    fun initialize() {
        OmniClientEventPassthrough.initialize()
        OmniCore.eventBus.on<HudRenderEvent> {
            if (!firstRefresh) {
                firstRefresh = true
                refresh0()
            }

            refresh()
            val matrixStack = UMatrixStack(
                //#if MC >= 1.16.5
                matrixStack.toVanillaStack()
                //#endif
            )
            window.draw(matrixStack)

        }
    }

    fun add(note: Note) {
        if (!note.isSticky) {
            return
        }

        note.createComponent() childOf noteContainer
    }

    fun remove(note: Note) {
        if (!note.isSticky) {
            return
        }

        noteContainer.childrenOfType<NoteComponent>().filter {
            it.note.uuid == note.uuid
        }.forEach(noteContainer::removeChild)
    }

    fun update(note: Note) {
        if (!note.isSticky) {
            return
        }

        remove(note)
        add(note)
    }

    fun move(note: Note, newX: Float, newY: Float) {
        if (!note.isSticky) {
            return
        }

        noteContainer.childrenOfType<NoteComponent>().filter {
            it.note.uuid == note.uuid
        }.forEach {
            it.constrain {
                x = newX.percent
                y = newY.percent
            }
        }
    }

    private fun refresh() {
        scheduledRefresh?.cancel(false)

        scheduledRefresh = refreshScheduler.schedule({
            Window.enqueueRenderOperation {
                refresh0()
            }
        }, UNRENDERED_REFRESH_TIME, java.util.concurrent.TimeUnit.MILLISECONDS)
    }

    private fun refresh0() {
        noteContainer.clearChildren()
        for (note in ClientNoteManager.get()) {
            if (!note.isSticky) {
                continue
            }

            note.createComponent() childOf noteContainer
        }
    }

    private fun Note.createComponent() = NoteComponent(this).constrain {
        x = this@createComponent.x.percent
        y = this@createComponent.y.percent
    } effect OutlineEffect(NoteablePalette.primary, 1f)

}
