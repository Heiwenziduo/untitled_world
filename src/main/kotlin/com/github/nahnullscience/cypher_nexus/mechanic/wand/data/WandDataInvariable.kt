package com.github.nahnullscience.cypher_nexus.mechanic.wand.data

import com.github.nahnullscience.cypher_nexus.init.ModDataComponents.WAND_HIGH_PAYLOAD
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.wand.WandProperties.CapacityRow
import com.github.nahnullscience.cypher_nexus.mechanic.wand.WandProperties.ManaMaxRow
import com.github.nahnullscience.cypher_nexus.mechanic.wand.WandProperties.ManaRegenRow
import com.github.nahnullscience.cypher_nexus.mechanic.wand.WandProperties.SpreadRow
import com.github.nahnullscience.cypher_nexus.mechanic.wand.WandProperties.WandCastDelayRow
import com.github.nahnullscience.cypher_nexus.mechanic.wand.WandProperties.WandDrawRow
import com.github.nahnullscience.cypher_nexus.mechanic.wand.WandProperties.WandRechargeTimeRow
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs.UUID_CODEC
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs.UUID_STREAM
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponentGetter
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.Item.TooltipContext
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipProvider
import java.util.*
import java.util.function.Consumer


/**
 * holds wand invariable data, separate into chunks
 * */
data class WandDataInvariable(
    val uuid: UUID,
    val chunkF: WandDataChunkF,
    val chunkI: WandDataChunkI,
    val chunkL: WandDataChunkL = WandDataChunkL(listOf()),
) : TooltipProvider {
    constructor(chunkF: WandDataChunkF, chunkI: WandDataChunkI) : this(UUID.randomUUID(), chunkF, chunkI)

    override fun addToTooltip(
        context: TooltipContext,
        consumer: Consumer<Component>,
        flag: TooltipFlag,
        components: DataComponentGetter
    ) {
        val aoc = components.getOrDefault(WAND_HIGH_PAYLOAD, WandDataHighPayload.EMPTY).aoc
        val a = Component.literal("this is a wand...").withStyle(ChatFormatting.GOLD)
        consumer.accept(a)

        val (manaMax, manaRegen, spread) = chunkF
        val (draw, delay, recharge) = chunkI

        consumer.accept(ManaMaxRow.row(manaMax).withStyle(ChatFormatting.GRAY))
        consumer.accept(ManaRegenRow.row(manaRegen).withStyle(ChatFormatting.GRAY))
        consumer.accept(CapacityRow.row(aoc.capacity).withStyle(ChatFormatting.GRAY))
        consumer.accept(WandDrawRow.row(draw).withStyle(ChatFormatting.GRAY))
        consumer.accept(WandCastDelayRow.row(delay).withStyle(ChatFormatting.GRAY))
        consumer.accept(WandRechargeTimeRow.row(recharge).withStyle(ChatFormatting.GRAY))
        consumer.accept(SpreadRow.row(spread).withStyle(ChatFormatting.GRAY))


        if (flag.hasControlDown()) {
            val u = Component.literal(uuid.toString().drop(0)).withStyle(ChatFormatting.UNDERLINE)

            consumer.accept(u)
        }
    }

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
                UUID.randomUUID(),
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
            UUID_CODEC.fieldOf("uuid").forGetter(WandDataInvariable::uuid),
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
            UUID_STREAM, WandDataInvariable::uuid,
            CHUNK0_STREAM, WandDataInvariable::chunkF,
            CHUNK1_STREAM, WandDataInvariable::chunkI,
            CHUNK2_STREAM, WandDataInvariable::chunkL,
            ::WandDataInvariable
        )


        val DEFAULT : WandDataInvariable
            // FIXME this only call once, can't gen random uuid
            get() {
                val data = WandDataInvariable(
                    chunkF = WandDataChunkF(300f, 3f, 4.5f),
                    chunkI = WandDataChunkI(1, 12, 15),
                )
                return data
            }

        val TEST_GOOD_WAND = WandDataInvariable(
            chunkF = WandDataChunkF(3000f, 30f, 7f),
            chunkI = WandDataChunkI(1, 6, 10),
        )

//        val SUB_INVOKER : WandDataInvariable = WandDataInvariable(
//            WandDataChunkF(1.0E9f, 0f, 0f),
//            WandDataChunkI(99, 1, 0, 0),
//            WandDataChunkL(listOf()),
//            WandDataChunkU(UUID.randomUUID().toString())
//        )

        val FALL_BACK = WandDataInvariable(
            chunkF = WandDataChunkF(-Float.MAX_VALUE, 0f, 0f),
            chunkI = WandDataChunkI(0, 0, 0),
        )
    }
}