package com.github.nahnullscience.cypher_nexus.datagen.loot_table

import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import java.util.concurrent.CompletableFuture

class CNLootTableProvider(
    output: PackOutput,
    registries: CompletableFuture<HolderLookup.Provider>
) : LootTableProvider(
    output,
    setOf(),
    listOf(
        SubProviderEntry(::CNChestLoot, LootContextParamSets.CHEST)
    ),
    registries
) {
    companion object {
//        private val LOCATIONS: MutableSet<ResourceKey<LootTable>> = HashSet<ResourceKey<LootTable>>()
//        private val IMMUTABLE_LOCATIONS: Set<ResourceKey<LootTable>> =
//            Collections.unmodifiableSet<ResourceKey<LootTable>>(LOCATIONS)
//        fun all() = IMMUTABLE_LOCATIONS
//
//        private fun register(path: String) = register(ResourceKey.create(Registries.LOOT_TABLE, CypherNexus.modResource(path)))
//        private fun register(key: ResourceKey<LootTable>): ResourceKey<LootTable> {
//            if (LOCATIONS.add(key)) { return key } else {
//                throw IllegalArgumentException(key.location().toString() + " is already a registered built-in loot table")
//            }
//        }
    }
}