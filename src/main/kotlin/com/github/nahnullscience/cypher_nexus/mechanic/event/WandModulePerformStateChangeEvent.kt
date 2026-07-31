//package com.github.nahnullscience.cypher_nexus.mechanic.event
//
//import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
//import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.WandModuleType
//import net.minecraft.world.entity.LivingEntity
//import net.neoforged.bus.api.ICancellableEvent
//import net.neoforged.neoforge.event.entity.living.LivingEvent
//
///**
// * fired when a living start / stop perform specific modules. fired on both sides.
// * this event is cancellable, if canceled, module state change won't apply.
// * the cancellation of the event won't prevent sending state to server
// *
// * Note: the cancellation should be consistent on both sides
// * @param module the module
// * @param state whether the module is going to perform or stop
// * */
//sealed class WandModulePerformStateChangeEvent (
//    entity: LivingEntity,
//    override val instance: ItemWandInstance?,
//    val module: WandModuleType<*>,
//    private val state: Boolean
//) : LivingEvent(entity), IWandInstanceEvent, ICancellableEvent {
//
//    fun isStart() = state
//    fun isEnd() = !state
//
//    class Start(
//        entity: LivingEntity,
//        instance: ItemWandInstance?,
//        module: WandModuleType<*>,
//    ) : WandModulePerformStateChangeEvent(entity, instance, module, true)
//
//    class End(
//        entity: LivingEntity,
//        instance: ItemWandInstance?,
//        module: WandModuleType<*>,
//    ) : WandModulePerformStateChangeEvent(entity, instance, module, false)
//}