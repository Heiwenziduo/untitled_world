package com.github.nahnullscience.cypher_nexus.datagen.loot_table.functions

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.init.ModDataLootFunctions.TIERED_WAND_GENERATION
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataInvariable
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

class TieredWandGenerationFunction(
    conditions: List<LootItemCondition>,
    private val tier: NumberProvider,
    private val preferences: Int,
//    private val invoke: ArrayOfCyphers? = null,
//    private val etch: List<AbstractCypher>? = null
) : LootItemConditionalFunction(conditions) {
    override fun getType() = TIERED_WAND_GENERATION.get()

    override fun run(stack: ItemStack, context: LootContext): ItemStack {
        if (stack.item !is IWandLike) return stack
        val random = context.random
        val tier = tier.getInt(context).coerceIn(0, 100)

        val preferenceMap = WandPropertyPreference.toProbabilityMap(preferences)
        val entry = WandPropertyPreference.entries
        val attrMap = HashMap<WandPropertyPreference, Int>(entry.size)

        var remainWeight = preferenceMap.entries.sumOf { it.value }
        var remainToken = 40 + tier * tier * 10

        // O(K) loop: iterate over the attributes, NOT the tokens
        // 10,000 tier wand generation will have the practically same performance as that of 1 tier
        // Gaussian Approximation
        for ((i, pre) in entry.withIndex()) {
            if (i == entry.size - 1) {
                attrMap[pre] = remainToken
                break
            }
            val weight = preferenceMap[pre] ?: 1
            val p = weight.toDouble() / remainWeight
            val mean = remainToken * p
            val variance = remainToken * p * (1 - p)
            val stdDev = kotlin.math.sqrt(variance)

            var allocated = (mean + random.nextGaussian() * stdDev).toInt()
            allocated = allocated.coerceIn(0, remainWeight)
            attrMap[pre] = allocated

            remainWeight -= weight
            remainToken -= allocated
        }

        val invariable = WandDataInvariable.builder()
            .manaMax(attrMap[WandPropertyPreference.MANA_MAX]?.toFloat()?.times(WandPropertyPreference.MANA_MAX.rate)?.plus(random.nextFloat() * WandPropertyPreference.MANA_MAX.rate) ?: 0f)
            .manaRegen(attrMap[WandPropertyPreference.MANA_REGEN]?.toFloat()?.times(WandPropertyPreference.MANA_REGEN.rate)?.plus(random.nextFloat() * WandPropertyPreference.MANA_REGEN.rate) ?: 0f)
            .capacity(attrMap[WandPropertyPreference.CAPA]?.times(WandPropertyPreference.CAPA.rate)?.plus(1)?.toInt() ?: 2)
            .draw(attrMap[WandPropertyPreference.DRAW]?.times(WandPropertyPreference.DRAW.rate)?.plus(1)?.toInt() ?: 1)
            .castDelay(attrMap[WandPropertyPreference.DELAY]?.times(WandPropertyPreference.DELAY.rate)?.toInt() ?: 0)
            .rechargeTime(attrMap[WandPropertyPreference.RECHARGE]?.times(WandPropertyPreference.RECHARGE.rate)?.toInt() ?: 0)
            .build()

        val highPayload = WandDataHighPayload(ArrayOfCyphers(invariable.chunkI.capacity))

        stack.set(ModDataComponents.WAND_INVARIABLE, invariable)
        stack.set(ModDataComponents.WAND_HIGH_PAYLOAD, highPayload) // TODO gen random aoc
//        stack.set(ModDataComponents.WAND_FREQUENT, WandDataFrequent.DEFAULT)

        CypherNexus.LOGGER.debug("tier{} wand is generated\ntoken{}\n{}", tier, attrMap,invariable)
        return stack
    }

    companion object {
        fun withTierAndPreference(tier: NumberProvider, preferences: Int, invoke: ArrayOfCyphers? = null, etch: List<AbstractCypher>? = null)
        : Builder<*> = simpleBuilder { conditions -> TieredWandGenerationFunction(conditions, tier, preferences, ) }

        val CODEC: MapCodec<TieredWandGenerationFunction> = RecordCodecBuilder.mapCodec {
            commonFields(it)
                .and(it.group(
                    NumberProviders.CODEC.fieldOf("tier").forGetter(TieredWandGenerationFunction::tier),
                    Codec.INT.fieldOf("preferences").orElse(0).forGetter(TieredWandGenerationFunction::preferences),
//                    AOC_CODEC.fieldOf("invoke").orElse(null).forGetter(TieredWandGenerationFunction::invoke),
//                    CYPHER_LIST.fieldOf("etch").orElse(null).forGetter(TieredWandGenerationFunction::etch)
                ))
                .apply(it, ::TieredWandGenerationFunction)

        }
    }

    enum class WandPropertyPreference(
        override val value: Int,

        val rate: Float,
    ): IFlaggable.IFlagEnum {
//        NO_PREFERENCE(0),
        MANA_MAX(1, 9f),
        MANA_REGEN(2, 1f),
        CAPA(4, 1f), // TODO wait to be balanced
        DRAW(8, 0.5f),
        DELAY(16, 1f),
        RECHARGE(32, 1f),

        ;

        companion object {
            fun preference(vararg pre: WandPropertyPreference): Int = pre.sumOf { a -> a.value }
            fun toProbabilityMap(preference: Int) : HashMap<WandPropertyPreference, Int> {
                val all = WandPropertyPreference.entries
                val map = HashMap<WandPropertyPreference, Int>(all.size)
                for (p in all) {
                    map[p] = if (preference.contain(p)) 3 else 1
                }
                return map
            }
        }
    }
}

private fun Int.contain(flag: TieredWandGenerationFunction.WandPropertyPreference): Boolean {
    return this and flag.value > 0
}
