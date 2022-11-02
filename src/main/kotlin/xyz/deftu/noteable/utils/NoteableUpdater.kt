package xyz.deftu.noteable.utils

import xyz.deftu.lib.updater.UpdaterEntrypoint
import xyz.deftu.noteable.config.Config

object NoteableUpdater : UpdaterEntrypoint {
    override fun shouldCheck() = Config.INSTANCE.updateChecking
}
