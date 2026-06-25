package com.github.nahnullscience.cypher_nexus.mechanic.event

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.WandModuleType
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.neoforged.neoforge.common.NeoForge

/**
 * events on both sides
 * */
object CNCommonEvents {
    /**
     * @return false if the event is canceled
     * */
    // TODO
    fun canConductWand(player: Player, wand: ItemStack, hand: InteractionHand, level: Level): Boolean {
        val event = NeoForge.EVENT_BUS.post(PlayerConductsWandEvent(player, wand, hand, level))
        return !event.isCanceled
    }

    fun livingGatherWandsTracking(living: LivingEntity): LivingGatherWandsEvent.Tracking {
        return NeoForge.EVENT_BUS.post(LivingGatherWandsEvent.Tracking(living))
    }

    fun livingGatherWandsActive(living: LivingEntity): LivingGatherWandsEvent.Active {
        return NeoForge.EVENT_BUS.post(LivingGatherWandsEvent.Active(living))
    }

    /**
     * broadcast on both sides, note they should have the same result
     * @return can start
     * */
    fun wandModuleStart(type: WandModuleType<*>, level: Level, invoker: LivingEntity, instance: ItemWandInstance?): Boolean {
        val event = NeoForge.EVENT_BUS.post(
            WandModulePerformStateChangeEvent.Start(invoker, instance, type)
        )
        return !event.isCanceled
    }

    /**
     * broadcast on both sides, note they should have the same result
     * @return can end
     * */
    fun wandModuleEnd(type: WandModuleType<*>, level: Level, invoker: LivingEntity, instance: ItemWandInstance?): Boolean {
        val event = NeoForge.EVENT_BUS.post(
            WandModulePerformStateChangeEvent.End(invoker, instance, type)
        )
        return !event.isCanceled
    }
}