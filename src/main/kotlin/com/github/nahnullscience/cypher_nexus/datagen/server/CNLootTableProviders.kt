package com.github.nahnullscience.cypher_nexus.datagen.server

import com.github.nahnullscience.cypher_nexus.datagen.server.loot_table.TieredWandGenerationFunction
import com.github.nahnullscience.cypher_nexus.datagen.server.loot_table.TieredWandGenerationFunction.WandPropertyPreference.Companion.preference
import com.github.nahnullscience.cypher_nexus.init.ModItems.TIERED_WAND
import net.minecraft.core.HolderLookup
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.data.loot.LootTableSubProvider
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.storage.loot.BuiltInLootTables
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator
import java.util.function.BiConsumer

object CNLootTableProviders {

    val providers = listOf(
        LootTableProvider.SubProviderEntry(::WandGeneration, LootContextParamSets.EMPTY)
    )

    class WandGeneration(val lookupProvider: HolderLookup.Provider) : LootTableSubProvider {
        override fun generate(output: BiConsumer<ResourceKey<LootTable>, LootTable.Builder>) {
            return
            // this will overwrite vanilla loot table
            output.accept(
                BuiltInLootTables.ABANDONED_MINESHAFT,
                LootTable.lootTable()
                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1F)))
                    .withPool(
                        LootPool.lootPool()
                            .setRolls(UniformGenerator.between(1f, 3f))
//                    .setBonusRolls(ConstantValue.exactly(1))
//                    .apply()
                            // .`when`(WeatherCheck.weather().setRaining(true))
                            .add(EmptyLootItem.emptyItem().setWeight(29))
                            .add(
                                LootItem.lootTableItem(TIERED_WAND)
                                    .setWeight(1)
                                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0f)))
                                    .apply(TieredWandGenerationFunction.withTierAndPreference(UniformGenerator.between(1f, 5f), preference()))
                            )
                    )
            )
        }
    }
}