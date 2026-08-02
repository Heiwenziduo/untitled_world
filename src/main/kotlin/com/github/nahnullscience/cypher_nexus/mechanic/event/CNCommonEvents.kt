package com.github.nahnullscience.cypher_nexus.mechanic.event

import com.github.nahnullscience.cypher_nexus.mechanic.event.wand.WandCanPerformInputModuleEvent
import com.github.nahnullscience.cypher_nexus.mechanic.event.wand.WandPerformingStateChangeEvent
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.WandModuleType
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractWandModule
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
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

//    @PublishedApi
//    internal inline fun gatherWands(
//        living: LivingEntity,
//        eventFactory: GatherLivingWandsConstructor,
//        consumer: (index: Int, wand: ItemStack) -> Unit
//    ) {
//        val pool = PooledWandArray.poll()
//        val event = eventFactory(living, pool.array)
//        NeoForge.EVENT_BUS.post(event)
//        for (i in pool.array.indices) consumer(i, pool.array[i])
//        pool.recycle()
//    }

    @PublishedApi
    internal inline fun gatherWandsNoPool(
        living: LivingEntity,
        eventFactory: GatherLivingWandsConstructor,
        consumer: (index: Int, wand: ItemStack) -> Unit
    ) {
        val array = ReferenceArrayList<ItemStack>()
        val event = eventFactory(living, array)
        NeoForge.EVENT_BUS.post(event)
        for (i in array.indices) consumer(i, array[i])
    }

    inline fun livingGatherWandsTracking(living: LivingEntity, consumer: (index: Int, wand: ItemStack) -> Unit) {
        gatherWandsNoPool(living, LivingGatherWandsEvent::Tracking, consumer)
    }

    inline fun livingGatherWandsActive(living: LivingEntity, consumer: (index: Int, wand: ItemStack) -> Unit) {
        gatherWandsNoPool(living, LivingGatherWandsEvent::Active, consumer)
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