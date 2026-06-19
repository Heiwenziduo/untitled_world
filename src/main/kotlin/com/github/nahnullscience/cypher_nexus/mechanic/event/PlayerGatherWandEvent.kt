package com.github.nahnullscience.cypher_nexus.mechanic.event

import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.event.entity.player.PlayerEvent

/** fired on both sides */
sealed class PlayerGatherWandEvent(
    val player: Player,
) : PlayerEvent(player) {
    private val wands = mutableListOf<ItemStack>()

    fun wands(): List<ItemStack> = wands
    fun addWand(wand: ItemStack) {
        // TODO check uuid uniqueness
        wands.add(wand)
    }

    /**
     * fired on both sides, collect wands that can be ticked
     * */
    class Tracking(player: Player) : PlayerGatherWandEvent(player)
    /**
     * fired on both sides, currently only main-hand and off-hand wands, will be rendered as overlay
     * */
    class Active(player: Player) : PlayerGatherWandEvent(player)
}