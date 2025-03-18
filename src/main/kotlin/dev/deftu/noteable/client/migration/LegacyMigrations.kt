package dev.deftu.noteable.client.migration

import dev.deftu.noteable.NoteableConstants
import dev.deftu.omnicore.common.OmniLoader
import java.io.File

object LegacyMigrations {

    val migrationsDir: File by lazy {
        val configDir = File(OmniLoader.configDir.toFile(), NoteableConstants.NAME)
        if (!configDir.exists() && !configDir.mkdirs()) {
            throw IllegalStateException("Could not create Noteable config directory!")
        }

        val migrationsDir = File(configDir, "migrations")
        if (!migrationsDir.exists() && !migrationsDir.mkdirs()) {
            throw IllegalStateException("Could not create Noteable migrations directory!")
        }

        migrationsDir
    }

    fun migrate() {
        //#if MC >= 1.18.2 && MC <= 1.19.2
        V1_0_Migrator.migrate()
        V1_2_Migrator.migrate()
        //#endif
    }

}
