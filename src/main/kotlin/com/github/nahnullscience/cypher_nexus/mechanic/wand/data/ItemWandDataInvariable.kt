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
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.IWandData.WandDataChunkF
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.IWandData.WandDataChunkI
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.IWandData.WandDataChunkL
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs.UUID_CODEC
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs.UUID_STREAM
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponentGetter
import net.minecraft.network.RegistryFriendlyByteBuf
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
data class ItemWandDataInvariable(
    val uuid: UUID,
    override val chunkF: WandDataChunkF,
    override val chunkI: WandDataChunkI,
    val chunkL: WandDataChunkL = WandDataChunkL(listOf()),
) : TooltipProvider, IWandData {
    constructor(chunkF: WandDataChunkF, chunkI: WandDataChunkI) : this(UUID.randomUUID(), chunkF, chunkI)

    override fun addToTooltip(
        context: TooltipContext,
        consumer: Consumer<Component>,
        flag: TooltipFlag,
        components: DataComponentGetter
    ) {
        val a = Component.literal("this is a wand...").withStyle(ChatFormatting.GOLD)
        consumer.accept(a)

        if (this == TO_BE_GENERATED) return
        if (this == FALL_BACK) {
            consumer.accept(Component.literal("missing data").withStyle(ChatFormatting.DARK_GRAY))
            return
        }

        val aoc = components.getOrDefault(WAND_HIGH_PAYLOAD, WandDataHighPayload.EMPTY).aoc
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
            val u = Component.literal(uuid.toString().slice(0..12))
                .withStyle(ChatFormatting.UNDERLINE)

            consumer.accept(u)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ItemWandDataInvariable) return false
        return this.uuid == other.uuid
    }

    override fun hashCode(): Int {
        val result = uuid.hashCode()
//        result = 31 * result + chunkF.hashCode()
//        result = 31 * result + chunkI.hashCode()
//        result = 31 * result + chunkL.hashCode()
        return result
    }

    companion object {
        fun builder() = Builder()

        fun default() = ItemWandDataInvariable(
            chunkF = WandDataChunkF(1000f, 10f, 10f),
            chunkI = WandDataChunkI(1, 10, 20),
        )

        fun good() = ItemWandDataInvariable(
            chunkF = WandDataChunkF(5000f, 50f, 5f),
            chunkI = WandDataChunkI(1, 5, 15),
        )

//        val SUB_INVOKER : WandDataInvariable = WandDataInvariable(
//            WandDataChunkF(1.0E9f, 0f, 0f),
//            WandDataChunkI(99, 1, 0, 0),
//            WandDataChunkL(listOf()),
//            WandDataChunkU(UUID.randomUUID().toString())
//        )

        val FALL_BACK = ItemWandDataInvariable(
            uuid = UUID(Long.MAX_VALUE, Long.MAX_VALUE),
            chunkF = WandDataChunkF(-Float.MAX_VALUE, 0f, 0f),
            chunkI = WandDataChunkI(0, 0, 0),
        )

        val TO_BE_GENERATED = ItemWandDataInvariable(
            uuid = UUID(Long.MIN_VALUE, Long.MIN_VALUE),
            chunkF = WandDataChunkF(0f, 0f, 0f),
            chunkI = WandDataChunkI(0, 0, 0),
        )

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

        val INVARIABLE_DATA_CODEC: Codec<ItemWandDataInvariable> = RecordCodecBuilder.create { it.group(
            UUID_CODEC.fieldOf("uuid").forGetter(ItemWandDataInvariable::uuid),
            CHUNK0_CODEX.fieldOf("chunkF").forGetter(ItemWandDataInvariable::chunkF),
            CHUNK1_CODEX.fieldOf("chunkI").forGetter(ItemWandDataInvariable::chunkI),
            CHUNK2_CODEX.fieldOf("chunkL").forGetter(ItemWandDataInvariable::chunkL),
        ).apply(it, ::ItemWandDataInvariable) }


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

        val INVARIABLE_DATA_STREAM: StreamCodec<RegistryFriendlyByteBuf, ItemWandDataInvariable> = StreamCodec.composite(
            UUID_STREAM, ItemWandDataInvariable::uuid,
            CHUNK0_STREAM, ItemWandDataInvariable::chunkF,
            CHUNK1_STREAM, ItemWandDataInvariable::chunkI,
            CHUNK2_STREAM, ItemWandDataInvariable::chunkL,
            ::ItemWandDataInvariable
        )
    }

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

        fun build() : ItemWandDataInvariable {
            return ItemWandDataInvariable(
                UUID.randomUUID(),
                WandDataChunkF(manaMax, manaRegen, spread),
                WandDataChunkI(draw, castDelay, rechargeTime),
                WandDataChunkL(alwaysInvoke),
            )
        }
    }
}