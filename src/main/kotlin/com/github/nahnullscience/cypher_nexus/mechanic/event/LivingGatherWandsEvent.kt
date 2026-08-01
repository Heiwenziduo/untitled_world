package com.github.nahnullscience.cypher_nexus.mechanic.event

import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike.Companion.isWand
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

    /**
     * contains wand valid check
     * */
    fun addIfWand(stack: ItemStack) {
        // TODO check uuid uniqueness
        if (stack.isWand()) list.add(stack)
    }

    /**
     * fired on both sides, collect wands that can be ticked.
     * if the living is not a player, the result generally is same as [Active]
     * */
    class Tracking(living: LivingEntity) : LivingGatherWandsEvent(living) {
        init {
            if (living is Player) {
                // collect wands in hotbar & offhand if player
                val offHand = living.inventory.getItem(Inventory.SLOT_OFFHAND)
                addIfWand(offHand)

                for (i in 0 until 9) {
                    val stack = living.inventory.getItem(i)
                    addIfWand(stack)
                }
            } else {
                // otherwise only on both hands
                InteractionHand.entries.forEach { hand ->
                    addIfWand(living.getItemInHand(hand))
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
                addIfWand(living.getItemInHand(hand))
            }
        }
    }
}