package dev.deftu.noteable.client.gui

import dev.deftu.noteable.client.ClientNoteManager
import dev.deftu.noteable.client.gui.components.NoteComponent
import dev.deftu.noteable.note.Note
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.UMatrixStack
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture

//#if FABRIC
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
//#elseif FORGE
//$$ import net.minecraftforge.common.MinecraftForge
//#if MC >= 1.19.2
//$$ import net.minecraftforge.client.event.RenderGuiEvent
//#else
//$$ import net.minecraftforge.client.event.RenderGameOverlayEvent
//#endif
//#if MC <= 1.12.2
//$$ import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
//#endif
//#else
//$$ import net.neoforged.neoforge.client.event.RenderGuiEvent
//$$ import net.neoforged.neoforge.common.NeoForge
//#endif

//#if MC >= 1.20.1
//$$ import net.minecraft.client.gui.DrawContext
//#elseif MC >= 1.16.5
import net.minecraft.client.util.math.MatrixStack
//#endif

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
        //#if FABRIC
        HudRenderCallback.EVENT.register { matrixStack, tickDelta ->
            render(matrixStack)
        }
        //#else
        //#if MC >= 1.19.2
        //#if FORGE
        //$$ MinecraftForge
        //#else
        //$$ NeoForge
        //#endif
        //$$     .EVENT_BUS.addListener<RenderGuiEvent.Post> { event ->
        //$$     render(
        //#if MC >= 1.20.1
        //$$         event.guiGraphics
        //#else
        //$$         event.poseStack
        //#endif
        //$$     )
        //$$ }
        //#elseif MC >= 1.16.5
        //$$ MinecraftForge.EVENT_BUS.addListener<RenderGameOverlayEvent> { event -> render(event.matrixStack) }
        //#else
        //$$ MinecraftForge.EVENT_BUS.register(this)
        //#endif
        //#endif
    }

    //#if FORGE && MC <= 1.12.2
    //$$ @SubscribeEvent
    //$$ fun onRenderOverlay(event: RenderGameOverlayEvent.Post) {
    //$$     if (event.type != RenderGameOverlayEvent.ElementType.ALL) return
    //$$
    //$$     render()
    //$$ }
    //#endif

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

    private fun render(
        //#if MC >= 1.20.1
        //$$ ctx: DrawContext
        //#elseif MC >= 1.16.5
        ctx: MatrixStack
        //#endif
    ) {
        if (!firstRefresh) {
            firstRefresh = true
            refresh0()
        }

        refresh()
        window.draw(UMatrixStack(
            //#if MC >= 1.20.1
            //$$ ctx.matrices
            //#elseif MC >= 1.16.5
            ctx
            //#endif
        ))
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
