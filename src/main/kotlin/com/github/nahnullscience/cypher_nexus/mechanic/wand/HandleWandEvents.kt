package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_MODULE_STATE_TRACKER
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.inputModules
import com.github.nahnullscience.cypher_nexus.mechanic.event.CNCommonEvents
import com.github.nahnullscience.cypher_nexus.mechanic.event.wand.WandPerformingStateChangeEvent
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IItemWand.Companion.wandInstanceOrNull
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
        val living = event.entity as? LivingEntity ?: return
        val tracker = living.getExistingDataOrNull(WAND_MODULE_STATE_TRACKER) ?: return

        val inputs = inputModules.filter { tracker.isPerforming(it) }.toMutableList()
        if (inputs.isNotEmpty()) {
            CNCommonEvents.livingGatherWandsActive(living) active@ { index, stack ->
                val instance = stack.wandInstanceOrNull(living) ?: run {
                    CypherNexus.debugWand(Level.ERROR) { "ItemStack: $stack is not a wand! why it's in the active wand list?" }
                    return@active
                }

                inputs.removeIf { type ->
                    val module = instance.getModule(type) ?: return@removeIf false
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
            CNCommonEvents.livingGatherWandsActive(invoker) { index, stack ->
                stack.wandInstanceOrNull(invoker)?.getModule(type)?.let { module ->
                    module.onHoldingStart(level, invoker, stack)
                    if (module.stopBubble) return@run
                }
            }
        }
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
            CNCommonEvents.livingGatherWandsActive(invoker) { index, stack ->
                stack.wandInstanceOrNull(invoker)?.getModule(type)?.let { module ->
                    if (module.isHolding) module.onHoldingStop(level, invoker, stack)
                }
            }
        }
    }


    /**
     * tick player tracking wands and perform basic logic, like generating mana.
     * other mobs counterpart are handled through [net.minecraft.world.item.Item.inventoryTick]
     * */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    private fun wandInstanceUpdatePlayer(event: PlayerTickEvent.Post) {
        val player = event.entity
        val map = player.getData(ModDataAttachments.WAND_INSTANCE_MAP)
        CNCommonEvents.livingGatherWandsTracking(player) track@ { index, stack ->
            val wand = stack.item as? IItemWand ?: return@track
            map.getOrPutInstance(player.level(), stack, wand).tick(player)
        }
    }

}
