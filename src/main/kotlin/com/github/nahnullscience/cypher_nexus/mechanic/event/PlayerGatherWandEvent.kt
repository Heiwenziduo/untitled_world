package com.github.nahnullscience.cypher_nexus.mechanic.event

import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.event.entity.player.PlayerEvent

sealed class PlayerGatherWandEvent(
    val player: Player,
) : PlayerEvent(player) {
    private val wands = mutableListOf<ItemStack>()

    fun wands(): List<ItemStack> = wands
    fun addWand(wand: ItemStack) {
        // TODO check uuid uniqueness
        wands.add(wand)
    }

    /** on both sides */
    class Ticking(player: Player) : PlayerGatherWandEvent(player)
    /** only client side */
    class Rendering(player: Player) : PlayerGatherWandEvent(player)
}