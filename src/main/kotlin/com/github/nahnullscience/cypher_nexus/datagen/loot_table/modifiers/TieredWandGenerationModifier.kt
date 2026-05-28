package com.github.nahnullscience.cypher_nexus.datagen.loot_table.modifiers

import com.mojang.serialization.MapCodec
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.neoforged.neoforge.common.loot.IGlobalLootModifier
import net.neoforged.neoforge.common.loot.LootModifier

class TieredWandGenerationModifier(conditionsIn: Array<LootItemCondition>) : LootModifier(conditionsIn) {
    override fun doApply(
        generatedLoot: ObjectArrayList<ItemStack?>,
        context: LootContext
    ): ObjectArrayList<ItemStack?> {
        TODO("Not yet implemented")
    }

    override fun codec(): MapCodec<out IGlobalLootModifier?> {
        TODO("Not yet implemented")
    }
}