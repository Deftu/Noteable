package dev.deftu.noteable.note

import com.google.gson.annotations.SerializedName
import dev.deftu.omnicore.annotations.GameSide
import dev.deftu.omnicore.annotations.Side
import java.util.UUID

data class Note(
    val uuid: UUID,
    val title: String,
    val content: String,
    @SerializedName("sticky") @GameSide(Side.CLIENT) var isSticky: Boolean,
    @SerializedName("syncable") var isSyncable: Boolean,
    var x: Int,
    var y: Int
) {

}
