package com.github.nahnullscience.cypher_nexus.mechanic.event

import com.github.nahnullscience.cypher_nexus.mechanic.event.wand.WandCanPerformInputModuleEvent
import com.github.nahnullscience.cypher_nexus.mechanic.event.wand.WandPerformingStateChangeEvent
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.WandModuleType
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractWandModule
import net.minecraft.world.entity.LivingEntity
import net.neoforged.neoforge.common.NeoForge

/**
 * events on both sides
 * */
object CNCommonEvents {
//    /**
//     * @return false if the event is canceled
//     * */
//    fun canConductWand(player: Player, wand: ItemStack, hand: InteractionHand, level: Level): Boolean {
//        val event = NeoForge.EVENT_BUS.post(PlayerConductsWandEvent(player, wand, hand, level))
//        return !event.isCanceled
//    }

    fun livingGatherWandsTracking(living: LivingEntity): LivingGatherWandsEvent.Tracking {
        return NeoForge.EVENT_BUS.post(LivingGatherWandsEvent.Tracking(living))
    }

    // TODO gatherWandsActive should be pooled or cached
    fun livingGatherWandsActive(living: LivingEntity): LivingGatherWandsEvent.Active {
        return NeoForge.EVENT_BUS.post(LivingGatherWandsEvent.Active(living))
    }

    /**
     * broadcast on both sides, note they should have the same result.
     * @return true if living can perform the module
     * */
    fun <Module : AbstractWandModule> canPerformInputModule(invoker: LivingEntity, type: WandModuleType<Module>): Boolean {
        return NeoForge.EVENT_BUS.post(WandCanPerformInputModuleEvent(invoker, type)).let { !it.isCanceled }
    }

    fun <Module : AbstractWandModule> inputModuleStart(invoker: LivingEntity, type: WandModuleType<Module>) {
        NeoForge.EVENT_BUS.post(WandPerformingStateChangeEvent.Start(invoker, type))
    }

    fun <Module : AbstractWandModule> inputModuleEnd(invoker: LivingEntity, type: WandModuleType<Module>) {
        NeoForge.EVENT_BUS.post(WandPerformingStateChangeEvent.End(invoker, type))
    }
}