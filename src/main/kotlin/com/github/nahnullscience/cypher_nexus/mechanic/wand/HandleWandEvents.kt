package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.inputModules
import com.github.nahnullscience.cypher_nexus.mechanic.entity.WandModuleStateTracker.Companion.getModulePerformingHand
import com.github.nahnullscience.cypher_nexus.mechanic.entity.WandModuleStateTracker.Companion.isPerformingModule
import com.github.nahnullscience.cypher_nexus.mechanic.event.CNCommonEvents
import com.github.nahnullscience.cypher_nexus.mechanic.event.WandModulePerformStateChangeEvent
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.InputModule
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
    private fun wandModuleTick(event: EntityTickEvent.Pre) { // didn't find LivingTick, strange
        val living = event.entity as? LivingEntity ?: return
        val inputs = inputModules.toMutableList()
        inputs.removeIf { type -> !living.isPerformingModule(type) } // pre-filter

        if (inputs.isNotEmpty())
        CNCommonEvents.livingGatherWandsActive(living).wandsSequence().forEach { stack ->
            val instance = (stack.item as IWandLike).itemWandInstance(living.level(), living, stack)!!
            inputs.removeIf { type ->
                val module = instance.module(type) ?: return@removeIf false
                module.onHoldingTick(living.level(), living, stack, 0)
                module.consumeInput
            }
            if (inputs.isEmpty()) return
        }
    }


    /**
     *
     * */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    private fun onModuleStart(event: WandModulePerformStateChangeEvent.Start) {
        val living = event.entity
        val type = event.module

        // "handy" modules search hand and are consumed by hand
        if (
            type.resource == WandModuleTypes.PRIMARY_RESOURCE ||
            type.resource == WandModuleTypes.SECONDARY_RESOURCE
        ) {
            // FIXME hand check fail occasionally, (when open a screen)
            val hand = living.getModulePerformingHand(event.module).also {
                if (it == null) {
                    CypherNexus.debugWand(Level.ERROR)
                    { "$type does not exist on $living both hands, this should not happen!" }
                    return
                }
            }
            val stack = living.getItemInHand(hand!!)
            val instance = (stack.item as IWandLike).itemWandInstance(living.level(), living, stack)!!
            (instance.module(type) as InputModule).onHoldingStart(living.level(), living, stack)
        }

        // others forward through list
        else {
            CNCommonEvents.livingGatherWandsActive(living).wandsSequence()
                .forEach { stack ->
                    val instance = (stack.item as IWandLike).itemWandInstance(living.level(), living, stack) ?: return@forEach
                    val module = instance.module(type)
                    if (module is InputModule) {
                        module.onHoldingStart(living.level(), living, stack)
                        if (module.consumeInput) return
                    }
                }

        }
    }


    /**
     *
     * */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    private fun onModuleEnd(event: WandModulePerformStateChangeEvent.End) {
        val living = event.entity
        val type = event.module

        // "handy" modules search hand and are consumed by hand
        if (
            type.resource == WandModuleTypes.PRIMARY_RESOURCE ||
            type.resource == WandModuleTypes.SECONDARY_RESOURCE
        ) {
            val hand = living.getModulePerformingHand(event.module).also {
                if (it == null) {
                    CypherNexus.debugWand(Level.ERROR)
                    { "$type does not exist on $living both hands, this should not happen!" }
                    return
                }
            }
            val stack = living.getItemInHand(hand!!)
            val instance = (stack.item as IWandLike).itemWandInstance(living.level(), living, stack)!!
            // TODO using tick counts
            (instance.module(type) as InputModule).onHoldingStop(living.level(), living, stack, 0)
        }

        // others forward through list
        else {
            CNCommonEvents.livingGatherWandsActive(living).wandsSequence()
                .forEach { stack ->
                    val instance = (stack.item as IWandLike).itemWandInstance(living.level(), living, stack) ?: return@forEach
                    val module = instance.module(type)
                    if (module is InputModule) {
                        module.onHoldingStop(living.level(), living, stack, 0)
                        if (module.consumeInput) return
                    }
                }

        }
    }


    /**
     * tick the wand-data-map to perform GC
     * */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    private fun tickWandDataMap(event: EntityTickEvent.Post) {
        // fired on both sides
        if (event.entity.hasData(ModDataAttachments.WAND_DATA_MAP)) {
            event.entity.getData(ModDataAttachments.WAND_DATA_MAP).tick(event.entity)
        }
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
//            (wand.item as IWandLike).itemWandInstance(player.level(), player, wand)?.tick(player)
            map.getOrPutInstance(
                (wand.item as IWandLike).getWandData(wand, null) ?: return@forEach,
                (wand.item as IWandLike),
                player.level()
            ).tick(player)
        }
    }

}