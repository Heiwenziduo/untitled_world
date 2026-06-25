package com.github.nahnullscience.cypher_nexus.mechanic.event

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.event.entity.living.LivingEvent

sealed class LivingGatherWandsEvent(entity: LivingEntity) : LivingEvent(entity) {
    private val list = mutableListOf<ItemStack>()
    fun wands(): List<ItemStack> = list.toList()
    fun wandsSequence(): Sequence<ItemStack> = list.asSequence()

    fun addWand(wand: ItemStack) {
        // TODO check uuid uniqueness
        list.add(wand)
    }

    /**
     * fired on both sides, collect wands that can be ticked.
     * if the living is not a player, the result generally is same as [Active]
     * */
    class Tracking(living: LivingEntity) : LivingGatherWandsEvent(living)

    /**
     * fired on both sides, should be a subset of tracking wands.
     * currently only contains main-hand and off-hand wands, will be rendered as overlay
     * */
    class Active(living: LivingEntity) : LivingGatherWandsEvent(living)
}