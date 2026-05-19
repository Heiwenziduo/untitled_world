package com.github.nahnullscience.cypher_nexus.mechanic.wand.data

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

data class WandDataFrequent (val manaCurrent: Float, val index: Int, val delay: Int, val recharge: Int) {
    fun fromStart() = WandDataFrequent(manaCurrent, 0, delay, recharge)

    companion object {
        val FREQUENT_DATA_CODEC: Codec<WandDataFrequent> = RecordCodecBuilder.create { it.group(
            Codec.FLOAT.fieldOf("manaCurrent").forGetter(WandDataFrequent::manaCurrent),
            Codec.INT.fieldOf("index").forGetter(WandDataFrequent::index),
            Codec.INT.fieldOf("delay").forGetter(WandDataFrequent::delay),
            Codec.INT.fieldOf("recharge").forGetter(WandDataFrequent::recharge),
//            Codec.INT.fieldOf("rechargeTotal").forGetter(WandDataFrequent::rechargeTotal),
        ).apply(it, ::WandDataFrequent) }

        val FREQUENT_DATA_STREAM: StreamCodec<ByteBuf, WandDataFrequent> = StreamCodec.composite(
                ByteBufCodecs.FLOAT, WandDataFrequent::manaCurrent,
            ByteBufCodecs.INT, WandDataFrequent::index,
            ByteBufCodecs.INT, WandDataFrequent::delay,
            ByteBufCodecs.INT, WandDataFrequent::recharge,
//            ByteBufCodecs.INT, WandDataFrequent::rechargeTotal,
                ::WandDataFrequent)

        val DEFAULT = WandDataFrequent(0f, 0, 0, 0, )
    }
}