package com.github.nahnullscience.cypher_nexus.init

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.datagen.loot_table.functions.TieredWandGenerationFunction
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.util.function.Supplier


object ModDataLootFunctions {

    val DEFERRED_REGISTER: DeferredRegister<LootItemFunctionType<*>> =
        DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    val TIERED_WAND_GENERATION: Supplier<LootItemFunctionType<TieredWandGenerationFunction>> =
        DEFERRED_REGISTER.register("tiered_wand_generation")
        { -> LootItemFunctionType(TieredWandGenerationFunction.CODEC) }
}