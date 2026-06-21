package com.github.nahnullscience.cypher_nexus.mechanic.wand.module

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.ByIdMap
import java.util.function.IntFunction

enum class ModuleCategory {
    PRIMARY,
    SECONDARY,
    SPECIAL,

    RECOIL,
    MANA_SHIELD,

    ;

    companion object {
        /* @doc
         * when sending information across the network where an object is present on both sides,
         * an integer representing an id is sent.
         * Ids representing an object reduce the amount of information that need to be synced across the network.
         * Both enums and registries make use of this.
         * */

        val BY_ID : IntFunction<ModuleCategory> =
            ByIdMap.continuous(
                ModuleCategory::ordinal,
                entries.toTypedArray(),
                ByIdMap.OutOfBoundsStrategy.ZERO
            )
        val STREAM_CODEC: StreamCodec<ByteBuf, ModuleCategory> = ByteBufCodecs.idMapper(BY_ID, ModuleCategory::ordinal)
    }
}