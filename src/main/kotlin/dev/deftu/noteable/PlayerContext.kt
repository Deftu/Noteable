package dev.deftu.noteable

import net.minecraft.client.Minecraft
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
//? if <1.9 {
/*import net.minecraft.util.math.BlockPos
import kotlin.math.floor
*///?}

/** Where the player is and what's around them, written the way a note would mention it */
object PlayerContext {
    private val DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm", Locale.ENGLISH)

    fun coordinates(): String? {
        val player = Minecraft.getInstance().player ?: return null
        //? if <1.9 {
        /*return "${floor(player.x).toInt()}, ${floor(player.y).toInt()}, ${floor(player.z).toInt()}"
        *///?} else {
        val pos = player.blockPosition()
        return "${pos.x}, ${pos.y}, ${pos.z}"
        //?}
    }

    fun dimension(): String? {
        val mc = Minecraft.getInstance()
        //? if <1.9 {
        /*val name = mc.world?.dimension?.name ?: return null
        return name.replaceFirstChar { it.uppercase() }
        *///?} else {
        val level = mc.level ?: return null
        return keyName(level.dimension().toString()).let {
            when (it) {
                "overworld" -> "Overworld"
                "the_nether" -> "Nether"
                "the_end" -> "The End"
                else -> it.words()
            }
        }
        //?}
    }

    fun biome(): String? {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return null
        //? if <1.9 {
        /*val world = mc.world ?: return null
        return world.getBiome(BlockPos(player.x, player.y, player.z))?.name
        *///?} else {
        val level = mc.level ?: return null
        val key = level.getBiome(player.blockPosition()).unwrapKey().orElse(null) ?: return null
        return keyName(key.toString()).words()
        //?}
    }

    fun facing(): String? {
        val player = Minecraft.getInstance().player ?: return null
        //? if <1.9 {
        /*val direction = player.horizontalFacing
        *///?} else {
        val direction = player.direction
        //?}
        return direction.name.lowercase().replaceFirstChar { it.uppercase() }
    }

    fun place(): String? {
        val mc = Minecraft.getInstance()
        //? if <1.9 {
        /*mc.server?.worldName?.let { return "World: $it" }
        return mc.currentServerEntry?.ip?.let { "Server: $it" }
        *///?} else {
        mc.singleplayerServer?.worldData?.levelName?.let { return "World: $it" }
        return mc.currentServer?.ip?.let { "Server: $it" }
        //?}
    }

    fun playerName(): String {
        val mc = Minecraft.getInstance()
        //? if <1.9 {
        /*return mc.session.username
        *///?} else {
        return mc.user.name
        //?}
    }

    fun dateTime(): String = DATE_FORMAT.format(LocalDateTime.now())

    // A ResourceKey's toString is "ResourceKey[minecraft:dimension / minecraft:the_nether]" on every version, unlike its accessors
    private fun keyName(key: String): String = key.substringAfterLast(" / ").removeSuffix("]").substringAfter(':')

    private fun String.words(): String = split('_').joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
}
