package com.github.nahnullscience.cypher_nexus.mechanic.event

import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.neoforged.neoforge.common.NeoForge

object CNEvents {
    /**
     * @return false if the event is canceled
     * */
    // TODO
    fun canConductWand(player: Player, wand: ItemStack, hand: InteractionHand, level: Level): Boolean {
        val event = NeoForge.EVENT_BUS.post(PlayerConductsWandEvent(player, wand, hand, level))
        return !event.isCanceled
    }

    fun gatherWandsTracking(player: Player): PlayerGatherWandEvent.Tracking {
        return NeoForge.EVENT_BUS.post(PlayerGatherWandEvent.Tracking(player))
    }

    fun gatherWandsActive(player: Player): PlayerGatherWandEvent.Active {
        return NeoForge.EVENT_BUS.post(PlayerGatherWandEvent.Active(player))
    }
}