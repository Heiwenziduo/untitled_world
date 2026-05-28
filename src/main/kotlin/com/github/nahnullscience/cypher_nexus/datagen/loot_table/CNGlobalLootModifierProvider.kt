package com.github.nahnullscience.cypher_nexus.datagen.loot_table

import com.github.nahnullscience.cypher_nexus.CypherNexus
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider
import java.util.concurrent.CompletableFuture

class CNGlobalLootModifierProvider (
    output: PackOutput,
   registries: CompletableFuture<HolderLookup.Provider>,
) : GlobalLootModifierProvider(
    output,
    registries,
    CypherNexus.MOD_ID
) {
    override fun start() {
//        add(
//            "tiered_wand_generation_modifier",
//
//        )
    }
}