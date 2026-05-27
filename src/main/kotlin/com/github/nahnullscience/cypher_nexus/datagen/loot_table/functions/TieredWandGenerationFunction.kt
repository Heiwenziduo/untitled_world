package com.github.nahnullscience.cypher_nexus.datagen.loot_table.functions

import com.github.nahnullscience.cypher_nexus.init.ModDataLootFunctions.TIERED_WAND_GENERATION
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.network.CNCodecs.AOC_CODEC
import com.github.nahnullscience.cypher_nexus.network.CNCodecs.CYPHER_LIST
import com.github.nahnullscience.cypher_nexus.utility.i.IFlaggable
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders
import java.util.*

class TieredWandGenerationFunction(
    conditions: List<LootItemCondition>,
    private val tier: NumberProvider,
    private val preferences: Int,
    private val invoke: ArrayOfCyphers? = null,
    private val etch: List<AbstractCypher>? = null
) : LootItemConditionalFunction(conditions) {
    override fun getType() = TIERED_WAND_GENERATION.get()

    override fun run(stack: ItemStack, context: LootContext): ItemStack {
        if (stack.item !is IWandLike) return stack
        val tier = tier.getInt(context)
        val randomUuid = UUID.randomUUID().toString()



        return stack
    }

    companion object {
        fun withTierAndPreference(tier: NumberProvider, preferences: Int, invoke: ArrayOfCyphers? = null, etch: List<AbstractCypher>? = null)
        : Builder<*> = simpleBuilder { conditions -> TieredWandGenerationFunction(conditions, tier, preferences, invoke, etch) }

        val CODEC: MapCodec<TieredWandGenerationFunction> = RecordCodecBuilder.mapCodec {
            commonFields(it)
                .and(it.group(
                    NumberProviders.CODEC.fieldOf("tier").forGetter(TieredWandGenerationFunction::tier),
                    Codec.INT.fieldOf("preferences").orElse(0).forGetter(TieredWandGenerationFunction::preferences),
                    AOC_CODEC.fieldOf("invoke").orElse(null).forGetter(TieredWandGenerationFunction::invoke),
                    CYPHER_LIST.fieldOf("etch").orElse(null).forGetter(TieredWandGenerationFunction::etch)
                ))
                .apply(it, ::TieredWandGenerationFunction)

        }
    }

    enum class WandPropertyPreference(
        override val value: Int
    ): IFlaggable.IFlagEnum {
        NO_PREFERENCE(0),
        MANA_MAX(1),
        MANA_REGEN(2),
        CAPA(4),
        DRAW(8),
        DELAY(16),
        RECHARGE(32),

        ;

        companion object {
            fun preference(vararg pre: WandPropertyPreference): Int = pre.sumOf { a -> a.value }
        }
    }
}

private fun Int.contain(flag: TieredWandGenerationFunction.WandPropertyPreference): Boolean {
    return this and flag.value > 0
}
