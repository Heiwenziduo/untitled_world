package com.github.nahnullscience.cypher_nexus.mechanic.wand.data

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import java.util.UUID


/**
 * holds wand invariable data, separate into chunks
 * */
data class WandDataInvariable(val chunkF: WandDataChunkF, val chunkI: WandDataChunkI, val chunkL: WandDataChunkL, val chunkU: WandDataChunkU) {
    data class WandDataChunkF(val manaMax: Float, val manaRegen: Float, val wandLength: Float,)
    data class WandDataChunkI(val capacity: Int, val draw: Int, val castDelay: Int, val rechargeTime: Int,)
    data class WandDataChunkL(val alwaysCast: List<AbstractCypher>)
    data class WandDataChunkU(val uuid: String)

    class Builder() {
        var manaMax: Float = 99f
        var manaRegen: Float = 0.9f
        var wandLength: Float = 0.9f

        var capacity: Int = 2
        var draw: Int = 1
        var castDelay: Int = 10
        var rechargeTime: Int = 15

        val alwaysCast = mutableListOf<AbstractCypher>()

        fun manaMax(v: Float) : Builder = run { manaMax += v; this }
        fun manaRegen(v: Float) : Builder = run { manaRegen += v; this }
        fun wandLength(v: Float) : Builder = run { wandLength = v; this }
        fun capacity(v: Int) : Builder = run { capacity = v; this }
        fun draw(v: Int) : Builder = run { draw = v; this }
        fun castDelay(v: Int) : Builder = run { castDelay -= v; this }
        fun rechargeTime(v: Int) : Builder = run { rechargeTime -= v; this }

        fun build() : WandDataInvariable {
            return WandDataInvariable(
                WandDataChunkF(manaMax, manaRegen, wandLength),
                WandDataChunkI(capacity, draw, castDelay, rechargeTime),
                WandDataChunkL(alwaysCast),
                WandDataChunkU(UUID.randomUUID().toString())
            )
        }
    }

    companion object {
        fun builder() = Builder()

        val CHUNK0_CODEX: Codec<WandDataChunkF> = RecordCodecBuilder.create { it.group(
            Codec.FLOAT.fieldOf("manaMax").forGetter(WandDataChunkF::manaMax),
            Codec.FLOAT.fieldOf("manaRegen").forGetter(WandDataChunkF::manaRegen),
            Codec.FLOAT.fieldOf("wandLength").forGetter(WandDataChunkF::wandLength),
        ).apply(it, ::WandDataChunkF) }
        val CHUNK1_CODEX: Codec<WandDataChunkI> = RecordCodecBuilder.create { it.group(
            Codec.INT.fieldOf("capacity").forGetter(WandDataChunkI::capacity),
            Codec.INT.fieldOf("draw").forGetter(WandDataChunkI::draw),
            Codec.INT.fieldOf("castDelay").forGetter(WandDataChunkI::castDelay),
            Codec.INT.fieldOf("rechargeTime").forGetter(WandDataChunkI::rechargeTime),
        ).apply(it, ::WandDataChunkI) }
        val CHUNK2_CODEX: Codec<WandDataChunkL> = RecordCodecBuilder.create { it.group(
            Cyphers.REGISTRY
                .byNameCodec().listOf()
                .fieldOf("alwaysCast")
                .forGetter(WandDataChunkL::alwaysCast)
        ).apply(it, ::WandDataChunkL) }
        val CHUNK3_CODEX: Codec<WandDataChunkU> = RecordCodecBuilder.create { it.group(
            Codec.STRING.fieldOf("uuid").forGetter(WandDataChunkU::uuid)
        ).apply(it, ::WandDataChunkU) }

        val INVARIABLE_DATA_CODEC: Codec<WandDataInvariable> = RecordCodecBuilder.create { it.group(
            CHUNK0_CODEX.fieldOf("chunkF").forGetter(WandDataInvariable::chunkF),
            CHUNK1_CODEX.fieldOf("chunkI").forGetter(WandDataInvariable::chunkI),
            CHUNK2_CODEX.fieldOf("chunkL").forGetter(WandDataInvariable::chunkL),
            CHUNK3_CODEX.fieldOf("chunkU").forGetter(WandDataInvariable::chunkU)
        ).apply(it, ::WandDataInvariable) }


        val CHUNK0_STREAM: StreamCodec<ByteBuf, WandDataChunkF> = StreamCodec.composite(
            ByteBufCodecs.FLOAT, WandDataChunkF::manaMax,
            ByteBufCodecs.FLOAT, WandDataChunkF::manaRegen,
            ByteBufCodecs.FLOAT, WandDataChunkF::wandLength,
            ::WandDataChunkF)
        val CHUNK1_STREAM: StreamCodec<ByteBuf, WandDataChunkI> = StreamCodec.composite(
            ByteBufCodecs.INT, WandDataChunkI::capacity,
            ByteBufCodecs.INT, WandDataChunkI::draw,
            ByteBufCodecs.INT, WandDataChunkI::castDelay,
            ByteBufCodecs.INT, WandDataChunkI::rechargeTime,
            ::WandDataChunkI)
        val CHUNK2_STREAM: StreamCodec<RegistryFriendlyByteBuf, WandDataChunkL> =
            ByteBufCodecs.registry(Cyphers.RESOURCE_KEY).apply(ByteBufCodecs.list())
                .map(::WandDataChunkL, WandDataChunkL::alwaysCast)
        val CHUNK3_STREAM: StreamCodec<ByteBuf, WandDataChunkU> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, WandDataChunkU::uuid,
            ::WandDataChunkU
        )

        val INVARIABLE_DATA_STREAM: StreamCodec<RegistryFriendlyByteBuf, WandDataInvariable> = StreamCodec.composite(
            CHUNK0_STREAM, WandDataInvariable::chunkF,
            CHUNK1_STREAM, WandDataInvariable::chunkI,
            CHUNK2_STREAM, WandDataInvariable::chunkL,
            CHUNK3_STREAM, WandDataInvariable::chunkU,
            ::WandDataInvariable
        )


        val DEFAULT : WandDataInvariable
            // FIXME this only call once, can't gen random uuid
            get() {
                val data = WandDataInvariable(
                    WandDataChunkF(300f, 3f, 1.2f),
                    WandDataChunkI(6, 1, 12, 15),
                    WandDataChunkL(listOf()),
                    WandDataChunkU(UUID.randomUUID().toString())
                )
                println(data.chunkU)
                return data
            }

//        val SUB_INVOKER : WandDataInvariable = WandDataInvariable(
//            WandDataChunkF(1.0E9f, 0f, 0f),
//            WandDataChunkI(99, 1, 0, 0),
//            WandDataChunkL(listOf()),
//            WandDataChunkU(UUID.randomUUID().toString())
//        )


    }
}