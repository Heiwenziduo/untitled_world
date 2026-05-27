package com.github.nahnullscience.cypher_nexus.datagen.loot_table

import com.github.nahnullscience.cypher_nexus.datagen.loot_table.functions.TieredWandGenerationFunction
import com.github.nahnullscience.cypher_nexus.datagen.loot_table.functions.TieredWandGenerationFunction.WandPropertyPreference.Companion.preference
import com.github.nahnullscience.cypher_nexus.init.ModItems.TIERED_WAND
import net.minecraft.core.HolderLookup
import net.minecraft.data.loot.LootTableSubProvider
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Items
import net.minecraft.world.level.storage.loot.BuiltInLootTables
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator
import java.util.function.BiConsumer

data class CNChestLoot(val registries: HolderLookup.Provider): LootTableSubProvider {
    override fun generate(output: BiConsumer<ResourceKey<LootTable>, LootTable.Builder>) {
        for (key in BuiltInLootTables.all()) {
            if (key.location().path.startsWith("chests")) {
                output.accept(
                    key,
                    LootTable.lootTable()
                        .withPool(
                            LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0f))
                            .add(LootItem.lootTableItem(Items.HEART_OF_THE_SEA)) // test
                            // TODO check first open
                        )
                )
            }
        }
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