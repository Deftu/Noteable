package dev.deftu.noteable

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.deftu.noteable.config.NoteableConfig
import dev.deftu.noteable.gui.NotesPage
import dev.deftu.noteable.gui.NoteHudInput
import dev.deftu.noteable.gui.NoteHuds
import dev.deftu.noteable.note.LegacyMigration
import dev.deftu.noteable.note.Note
import dev.deftu.noteable.note.NoteLibrary
import net.fabricmc.api.ClientModInitializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.polyfrost.oneconfig.api.commands.v1.CommandManager
import org.polyfrost.oneconfig.api.ui.v1.ExperimentalOneConfigUiApi
import org.polyfrost.oneconfig.api.ui.v1.page.OneConfigPages

object Noteable : ClientModInitializer {
    const val NAME = "@MOD_NAME@"
    const val ID = "@MOD_ID@"
    const val ICON = "/assets/noteable/noteable.svg"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(NAME)

    @OptIn(ExperimentalOneConfigUiApi::class)
    override fun onInitializeClient() {
        NoteableConfig.preload()
        NoteLibrary.load()
        LegacyMigration.run()
        NoteHuds.register()
        NoteHudInput.register()
        if (OneConfigPages.byId(NotesPage.id) == null) OneConfigPages.register(NotesPage)
        CommandManager.register(command(ID))
        CommandManager.register(command("notes"))
    }

    private fun command(name: String): LiteralArgumentBuilder<*> {
        fun withNote(action: String, run: (Note) -> Unit) = CommandManager.literal(action)
            .then(CommandManager.argument("note", StringArgumentType.greedyString()).executes { ctx ->
                val note = NoteLibrary.find(StringArgumentType.getString(ctx, "note"))
                if (note == null) {
                    NoteActions.notifyError("Note not found", "No note matches that name or id.")
                    0
                } else {
                    run(note)
                    1
                }
            })

        fun title(ctx: CommandContext<*>) = StringArgumentType.getString(ctx, "title")

        return CommandManager.literal(name)
            .executes { NoteActions.openNotes(); 1 }
            .then(CommandManager.literal("new")
                .executes { NoteActions.openNotes(NoteActions.create()); 1 }
                .then(CommandManager.argument("title", StringArgumentType.greedyString()).executes { ctx ->
                    NoteActions.announceCreated(NoteActions.create(title(ctx)))
                    1
                }))
            .then(CommandManager.literal("list").executes { NoteActions.sendList(); 1 })
            .then(CommandManager.literal("folder").executes { NoteActions.openFolder(); 1 })
            .then(withNote("open") { NoteActions.openNotes(it) })
            .then(withNote("pin") { NoteActions.setPinned(it, !NoteHuds.isPinned(it)) })
            .then(withNote("copy") { NoteActions.copy(it) })
    }
}
