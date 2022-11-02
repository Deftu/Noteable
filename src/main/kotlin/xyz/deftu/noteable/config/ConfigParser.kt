package xyz.deftu.noteable.config

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.StringReader
import java.io.StringWriter

object ConfigParser {
    fun parse(input: String): Config {
        JsonReader(StringReader(input)).use { reader ->
            val token = reader.peek()
            if (token != JsonToken.BEGIN_OBJECT)
                throw InvalidConfigException("Config should be a JSON object!")

            var enabled = true
            var updateChecking = true

            reader.beginObject()

            var currentName = ""
            while (reader.hasNext()) {
                val token = reader.peek()
                if (token == JsonToken.NAME) {
                    currentName = reader.nextName()
                    continue
                }

                when (currentName) {
                    "enabled" -> {
                        if (token != JsonToken.BOOLEAN)
                            throw InvalidConfigException("Config value 'enabled' should be a boolean!")
                        enabled = reader.nextBoolean()
                    }
                    "update_checking" -> {
                        if (token != JsonToken.BOOLEAN)
                            throw InvalidConfigException("Config value 'update_checking' should be a boolean!")
                        updateChecking = reader.nextBoolean()
                    }
                    else -> reader.skipValue()
                }
            }

            reader.endObject()
            return Config(enabled, updateChecking)
        }
    }

    fun serialize(config: Config): String {
        val writer = StringWriter()

        JsonWriter(writer).use { writer ->
            writer.beginObject()
            writer.name("enabled").value(config.enabled)
            writer.name("update_checking").value(config.updateChecking)
            writer.endObject()
        }

        return writer.toString()
    }
}
