package com.github.nahnullscience.cypher_nexus.mechanic.wand.data

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

data class WandDataFrequent
    (val manaCurrent: Float, val index: Int, val delay: Int, val recharge: Int, val deck: Long, val discard: Long) {
    fun fromStart() = WandDataFrequent(manaCurrent, 0, delay, recharge, 0, 0)

    companion object {
        val FREQUENT_DATA_CODEC: Codec<WandDataFrequent> = RecordCodecBuilder.create { it.group(
            Codec.FLOAT.fieldOf("manaCurrent").forGetter(WandDataFrequent::manaCurrent),
            Codec.INT.fieldOf("index").forGetter(WandDataFrequent::index),
            Codec.INT.fieldOf("delay").forGetter(WandDataFrequent::delay),
            Codec.INT.fieldOf("recharge").forGetter(WandDataFrequent::recharge),
            Codec.LONG.fieldOf("deck").forGetter(WandDataFrequent::deck),
            Codec.LONG.fieldOf("discard").forGetter(WandDataFrequent::discard),
        ).apply(it, ::WandDataFrequent) }

        val FREQUENT_DATA_STREAM: StreamCodec<ByteBuf, WandDataFrequent> = StreamCodec.composite(
                ByteBufCodecs.FLOAT, WandDataFrequent::manaCurrent,
            ByteBufCodecs.INT, WandDataFrequent::index,
            ByteBufCodecs.INT, WandDataFrequent::delay,
            ByteBufCodecs.INT, WandDataFrequent::recharge,
            /* @doc
             * A VAR_LONG uses a compression algorithm (borrowed from Protocol Buffers) to save bandwidth.
             * It uses between 1 and 10 bytes depending on how large the mathematical value of the number is.
             *
             * # note that the max width of VAR_LONG is slightly bigger than a fixed-length-Long,
             * # this may result in a 25% waste when situation requires frequently passing big numbers (like 1L shl 60)
             * */
            ByteBufCodecs.VAR_LONG, WandDataFrequent::deck,
            ByteBufCodecs.VAR_LONG, WandDataFrequent::discard,
                ::WandDataFrequent)

        val DEFAULT = WandDataFrequent(0f, 0, 0, 0, 0, 0)
    }
}