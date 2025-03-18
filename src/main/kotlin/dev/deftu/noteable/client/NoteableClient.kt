package dev.deftu.noteable.client

import dev.deftu.noteable.NoteableConstants
import dev.deftu.noteable.client.gui.HudRenderer
import dev.deftu.noteable.client.gui.menu.NoteEditorMenu
import dev.deftu.noteable.client.migration.LegacyMigrations
import dev.deftu.omnicore.client.OmniClient
import dev.deftu.omnicore.client.OmniClientCommands
import dev.deftu.omnicore.client.OmniScreen
import org.apache.logging.log4j.LogManager
import java.util.Timer
import kotlin.concurrent.schedule

object NoteableClient {

    private val logger = LogManager.getLogger(NoteableClient::class.java)

    fun onInitializeClient() {
        logger.info("Initializing client-sided ${NoteableConstants.NAME} ${NoteableConstants.VERSION}")

        LegacyMigrations.migrate() // Migrate old notes to new format
        ClientNoteManager.initialize()
        HudRenderer.initialize()

        OmniClientCommands.register(OmniClientCommands.literal(NoteableConstants.ID).executes { ctx ->
            Timer().schedule(1L) {
                OmniClient.execute {
                    OmniScreen.currentScreen = NoteEditorMenu()
                }
            }

            1
        })
    }
}
