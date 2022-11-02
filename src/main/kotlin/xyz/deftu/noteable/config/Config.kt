package xyz.deftu.noteable.config

import com.google.gson.JsonObject
import xyz.deftu.noteable.Noteable
import java.io.File

data class Config(
    var enabled: Boolean = true,
    var updateChecking: Boolean = true
) {
    companion object {
        val FILE by lazy {
            val file = File(Noteable.getConfigDirectory(), "config.json")
            if (!file.exists()) {
                if (file.createNewFile()) {
                    file.writeText(JsonObject().toString())
                } else throw IllegalStateException("Could not create config file!")
            }

            file
        }

        val INSTANCE by lazy {
            ConfigParser.parse(FILE.readText())
        }
    }

    fun save() {
        FILE.writeText(ConfigParser.serialize(this))
    }
}
