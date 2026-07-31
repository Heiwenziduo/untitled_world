package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_MODULE_STATE_TRACKER
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.inputModules
import com.github.nahnullscience.cypher_nexus.mechanic.entity.WandModuleStateTracker.Companion.isPerformingModule
import com.github.nahnullscience.cypher_nexus.mechanic.event.CNCommonEvents
import com.github.nahnullscience.cypher_nexus.mechanic.event.wand.WandPerformingStateChangeEvent
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike.Companion.wandInstanceOrNull
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.WandModuleType
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractInputModule
import net.minecraft.world.entity.LivingEntity
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.tick.EntityTickEvent
import net.neoforged.neoforge.event.tick.PlayerTickEvent
import org.apache.logging.log4j.Level

@EventBusSubscriber(modid = CypherNexus.MOD_ID)
object HandleWandEvents {

    /**
     * perform modules, on both sides
     * */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    private fun wandModuleTick(event: EntityTickEvent.Pre) { // didn't find `LivingTick`, strange
        if (!event.entity.hasData(WAND_MODULE_STATE_TRACKER)) return
        val living = event.entity as? LivingEntity ?: return

        val inputs = inputModules.filter { living.isPerformingModule(it) }.toMutableList()
        if (inputs.isNotEmpty()) {
//            println("${living.level().sideString()} tick performing: $inputs")
            CNCommonEvents.livingGatherWandsActive(living).wandsSequence().forEach { stack ->
                val instance = stack.wandInstanceOrNull(living) ?: run {
                    CypherNexus.debugWand(Level.ERROR) { "ItemStack $stack is not a wand! why it's in the active wand list?" }
                    return@forEach
                }

                inputs.removeIf { type ->
                    val module = instance.getModule(type) ?: return@removeIf false
//                    println("tick holding $module")
                    module.onHoldingTick(living.level(), living, stack)
                    module.stopBubble
                }
                if (inputs.isEmpty()) return
            }
        }
    }


    /**
     * handles "what if a living starts to perform modules of a specific type", and don't care about what module is.
     * */
    @Suppress("UNCHECKED_CAST")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    private fun onModuleStart(event: WandPerformingStateChangeEvent.Start) {
        val type = event.type as? WandModuleType<AbstractInputModule> ?: return
        val invoker = event.entity
        val level = invoker.level()

        run {
            // should we strictly limit Primary & Secondary modules that can only be performed on Hands?
            CNCommonEvents.livingGatherWandsActive(invoker).wandsSequence().forEach { stack ->
                stack.wandInstanceOrNull(invoker)?.getModule(type)?.let { module ->
                    module.onHoldingStart(level, invoker, stack)
                    if (module.stopBubble) return@run
                }
            }
        }

//        println("onModuleStart: $type")
//        // "handy" modules search hand and are consumed by hand
//        if (
//            type.resource == WandModuleTypes.PRIMARY_RESOURCE ||
//            type.resource == WandModuleTypes.SECONDARY_RESOURCE
//        ) {
//            val hand = invoker.getModulePerformingHand(type).also {
//                if (it == null) {
//                    CypherNexus.debugWand(Level.ERROR)
//                    { "onModuleStart: $type does not exist on $invoker both hands, this should not happen!" }
//                    return
//                }
//            }
//            println("-----?")
//            val stack = invoker.getItemInHand(hand!!)
//            val instance = (stack.item as IWandLike).itemWandInstance(invoker.level(), invoker, stack)!!
//            (instance.getModule(type) as InputModule).onHoldingStart(invoker.level(), invoker, stack)
//        }
//
//        // others forward through list
//        else {
//            CNCommonEvents.livingGatherWandsActive(invoker).wandsSequence()
//                .forEach { stack ->
//                    val instance = (stack.item as IWandLike).itemWandInstance(invoker.level(), invoker, stack) ?: return@forEach
//                    val module = instance.getModule(type)
//                    if (module is InputModule) {
//                        module.onHoldingStart(invoker.level(), invoker, stack)
//                        if (module.consumeInput) return
//                    }
//                }
//
//        }
    }


    /**
     * handles "what if a living stops to perform modules of a specific type", and don't care about what module is.
     * */
    @Suppress("UNCHECKED_CAST")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    private fun onModuleEnd(event: WandPerformingStateChangeEvent.End) {
        val type = event.type as? WandModuleType<AbstractInputModule> ?: return
        val invoker = event.entity
        val level = invoker.level()

        run {
            CNCommonEvents.livingGatherWandsActive(invoker).wandsSequence().forEach { stack ->
                stack.wandInstanceOrNull(invoker)?.getModule(type)?.let { module ->
                    if (module.isHolding) module.onHoldingStop(level, invoker, stack)
                }
            }
        }

//        println("onModuleEnd")
//        // "handy" modules search hand and are consumed by hand
//        if (
//            type.resource == WandModuleTypes.PRIMARY_RESOURCE ||
//            type.resource == WandModuleTypes.SECONDARY_RESOURCE
//        ) {
//            val hand = living.getModulePerformingHand(type).also {
//                if (it == null) {
//                    CypherNexus.debugWand(Level.ERROR)
//                    { "onModuleEnd: $type does not exist on $living both hands, this should not happen!" }
//                    return
//                }
//            }
//            val stack = living.getItemInHand(hand!!)
//            val instance = (stack.item as IWandLike).itemWandInstance(living.level(), living, stack)!!
//            (instance.getModule(type) as InputModule).onHoldingStop(living.level(), living, stack, 0)
//        }
//
//        // others forward through list
//        else {
//            CNCommonEvents.livingGatherWandsActive(living).wandsSequence()
//                .forEach { stack ->
//                    val instance = (stack.item as IWandLike).itemWandInstance(living.level(), living, stack) ?: return@forEach
//                    val module = instance.getModule(type)
//                    if (module is InputModule) {
//                        module.onHoldingStop(living.level(), living, stack, 0)
//                        if (module.consumeInput) return
//                    }
//                }
//
//        }
    }


    /**
     * tick player tracking wands and perform basic logic, like generating mana.
     * other mobs counterpart are handled through [net.minecraft.world.item.Item.inventoryTick]
     * */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    private fun wandInstanceUpdatePlayer(event: PlayerTickEvent.Post) {
        val player = event.entity
        val map = player.getData(ModDataAttachments.WAND_DATA_MAP)
        val wands = CNCommonEvents.livingGatherWandsTracking(player).wands()
        wands.forEach { wand ->
            map.getOrPutInstance(
                (wand.item as IWandLike).getWandData(wand, null) ?: return@forEach,
                (wand.item as IWandLike),
                player.level()
            ).tick(player)
        }
    }

}