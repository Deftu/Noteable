package xyz.deftu.noteable.utils

import xyz.deftu.lib.updater.UpdaterEntrypoint
import xyz.deftu.noteable.NoteableConfig

object NoteableUpdater : UpdaterEntrypoint {
    override fun shouldCheck() = NoteableConfig.updateChecking
}
