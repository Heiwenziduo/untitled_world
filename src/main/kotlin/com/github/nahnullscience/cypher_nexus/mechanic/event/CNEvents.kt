package com.github.nahnullscience.cypher_nexus.mechanic.event

import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.neoforged.neoforge.common.NeoForge

object CNEvents {
    /** false if the event is canceled */
    fun canConductWand(player: Player, wand: ItemStack, hand: InteractionHand, level: Level): Boolean {
        val event = NeoForge.EVENT_BUS.post(PlayerConductsWandEvent(player, wand, hand, level))
        return !event.isCanceled
    }

    fun gatherTickingWands(player: Player): PlayerGatherWandEvent.Ticking {
        return NeoForge.EVENT_BUS.post(PlayerGatherWandEvent.Ticking(player))
    }

    fun gatherRenderingWands(player: Player): PlayerGatherWandEvent.Rendering {
        return NeoForge.EVENT_BUS.post(PlayerGatherWandEvent.Rendering(player))
    }
}