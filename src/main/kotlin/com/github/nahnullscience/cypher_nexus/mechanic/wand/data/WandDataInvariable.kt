package com.github.nahnullscience.cypher_nexus.mechanic.wand.data

import com.github.nahnullscience.cypher_nexus.CypherNexus
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
data class WandDataInvariable(
    val uuid: String,
    val chunkF: WandDataChunkF,
    val chunkI: WandDataChunkI,
    val chunkL: WandDataChunkL = WandDataChunkL(listOf()),
) {
    data class WandDataChunkF(val manaMax: Float, val manaRegen: Float, val spread: Float)
    data class WandDataChunkI(val draw: Int, val castDelay: Int, val rechargeTime: Int,)
    data class WandDataChunkL(val alwaysInvoke: List<AbstractCypher>)

    class Builder {
        var manaMax: Float = 100f
        var manaRegen: Float = 1f
        var spread: Float = 0f

        var draw: Int = 1
        var castDelay: Int = 10
        var rechargeTime: Int = 15

        val alwaysInvoke = mutableListOf<AbstractCypher>()

        fun manaMax(v: Float) : Builder = run { manaMax += v; this }
        fun manaRegen(v: Float) : Builder = run { manaRegen += v; this }
        fun spread(v: Float) : Builder = run { spread = v; this }

        fun draw(v: Int) : Builder = run { draw = v; this }
        fun castDelay(v: Int) : Builder = run { castDelay -= v; this }
        fun rechargeTime(v: Int) : Builder = run { rechargeTime -= v; this }

        fun build() : WandDataInvariable {
            return WandDataInvariable(
                UUID.randomUUID().toString(),
                WandDataChunkF(manaMax, manaRegen, spread),
                WandDataChunkI(draw, castDelay, rechargeTime),
                WandDataChunkL(alwaysInvoke),
                )
        }
    }

    companion object {
        fun builder() = Builder()

        val CHUNK0_CODEX: Codec<WandDataChunkF> = RecordCodecBuilder.create { it.group(
            Codec.FLOAT.fieldOf("manaMax").forGetter(WandDataChunkF::manaMax),
            Codec.FLOAT.fieldOf("manaRegen").forGetter(WandDataChunkF::manaRegen),
            Codec.FLOAT.fieldOf("spread").forGetter(WandDataChunkF::spread),
        ).apply(it, ::WandDataChunkF) }
        val CHUNK1_CODEX: Codec<WandDataChunkI> = RecordCodecBuilder.create { it.group(
            Codec.INT.fieldOf("draw").forGetter(WandDataChunkI::draw),
            Codec.INT.fieldOf("castDelay").forGetter(WandDataChunkI::castDelay),
            Codec.INT.fieldOf("rechargeTime").forGetter(WandDataChunkI::rechargeTime),
        ).apply(it, ::WandDataChunkI) }
        val CHUNK2_CODEX: Codec<WandDataChunkL> = RecordCodecBuilder.create { it.group(
            Cyphers.REGISTRY
                .byNameCodec().listOf()
                .fieldOf("alwaysInvoke")
                .forGetter(WandDataChunkL::alwaysInvoke)
        ).apply(it, ::WandDataChunkL) }

        val INVARIABLE_DATA_CODEC: Codec<WandDataInvariable> = RecordCodecBuilder.create { it.group(
            Codec.STRING.fieldOf("uuid").forGetter(WandDataInvariable::uuid),
            CHUNK0_CODEX.fieldOf("chunkF").forGetter(WandDataInvariable::chunkF),
            CHUNK1_CODEX.fieldOf("chunkI").forGetter(WandDataInvariable::chunkI),
            CHUNK2_CODEX.fieldOf("chunkL").forGetter(WandDataInvariable::chunkL),
        ).apply(it, ::WandDataInvariable) }


        val CHUNK0_STREAM: StreamCodec<ByteBuf, WandDataChunkF> = StreamCodec.composite(
            ByteBufCodecs.FLOAT, WandDataChunkF::manaMax,
            ByteBufCodecs.FLOAT, WandDataChunkF::manaRegen,
            ByteBufCodecs.FLOAT, WandDataChunkF::spread,
            ::WandDataChunkF)
        val CHUNK1_STREAM: StreamCodec<ByteBuf, WandDataChunkI> = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WandDataChunkI::draw,
            ByteBufCodecs.VAR_INT, WandDataChunkI::castDelay,
            ByteBufCodecs.VAR_INT, WandDataChunkI::rechargeTime,
            ::WandDataChunkI)
        val CHUNK2_STREAM: StreamCodec<RegistryFriendlyByteBuf, WandDataChunkL> =
            ByteBufCodecs.registry(Cyphers.RESOURCE_KEY).apply(ByteBufCodecs.list())
                .map(::WandDataChunkL, WandDataChunkL::alwaysInvoke)

        val INVARIABLE_DATA_STREAM: StreamCodec<RegistryFriendlyByteBuf, WandDataInvariable> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, WandDataInvariable::uuid,
            CHUNK0_STREAM, WandDataInvariable::chunkF,
            CHUNK1_STREAM, WandDataInvariable::chunkI,
            CHUNK2_STREAM, WandDataInvariable::chunkL,
            ::WandDataInvariable
        )


        val DEFAULT : WandDataInvariable
            // FIXME this only call once, can't gen random uuid
            get() {
                val data = WandDataInvariable(
                    UUID.randomUUID().toString(),
                    WandDataChunkF(300f, 3f, 0f),
                    WandDataChunkI(1, 12, 15),
                    WandDataChunkL(listOf()),

                )
                return data
            }

        val TEST_GOOD_WAND = WandDataInvariable(
            uuid   = "mythical",
            chunkF = WandDataChunkF(3000f, 30f, 7f),
            chunkI = WandDataChunkI(1, 6, 10),
        )

//        val SUB_INVOKER : WandDataInvariable = WandDataInvariable(
//            WandDataChunkF(1.0E9f, 0f, 0f),
//            WandDataChunkI(99, 1, 0, 0),
//            WandDataChunkL(listOf()),
//            WandDataChunkU(UUID.randomUUID().toString())
//        )

        const val DATA_FAIL_UUID = "fall_back"
        val FALL_BACK = WandDataInvariable(
            uuid   = DATA_FAIL_UUID,
            chunkF = WandDataChunkF(-Float.MAX_VALUE, 0f, 0f),
            chunkI = WandDataChunkI(0, 0, 0),
        )
    }
}