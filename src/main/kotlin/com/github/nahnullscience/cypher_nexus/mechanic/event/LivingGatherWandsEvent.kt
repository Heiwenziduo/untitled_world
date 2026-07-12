package com.github.nahnullscience.cypher_nexus.mechanic.event

import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
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
    class Tracking(living: LivingEntity) : LivingGatherWandsEvent(living) {
        init {
            if (living !is Player) {
                InteractionHand.entries.forEach { hand ->
                    val stack = living.getItemInHand(hand)
                    if (IWandLike.validateItemWand(stack)) addWand(stack)
                }
            } else {
                // collect wands in hotbar & offhand
                val offHand = living.inventory.getItem(Inventory.SLOT_OFFHAND)
                if (IWandLike.validateItemWand(offHand)) addWand(offHand)

                for (i in 0 until 9) {
                    val stack = living.inventory.getItem(i)
                    if (IWandLike.validateItemWand(stack)) addWand(stack)
                }
            }
        }
    }

    /**
     * fired on both sides, should be a subset of tracking wands.
     * currently only contains main-hand and off-hand wands, will be rendered as overlay
     * */
    class Active(living: LivingEntity) : LivingGatherWandsEvent(living) {
        init {
            InteractionHand.entries.forEach { hand ->
                val stack = living.getItemInHand(hand)
                if (IWandLike.validateItemWand(stack)) addWand(stack)
            }
        }
    }
}