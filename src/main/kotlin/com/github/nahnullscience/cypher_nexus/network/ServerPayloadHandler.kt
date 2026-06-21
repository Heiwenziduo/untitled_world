package com.github.nahnullscience.cypher_nexus.network

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_DATA_MAP
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.event.CNEvents
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.network.server.ServerboundEditWandCyphers
import com.github.nahnullscience.cypher_nexus.network.server.ServerboundPerformWandModule
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.handling.IPayloadContext
import org.apache.logging.log4j.Level

/** client counterpart resides in cypher_nexus.client.network */
object ServerPayloadHandler {
    fun editWandCyphers(data: ServerboundEditWandCyphers, context: IPayloadContext) {
        CypherNexus.debugNetwork { "server receive package -> editWandCyphers: \n$data" }

        context.enqueueWork {
            val player = context.player()
            var stack: ItemStack = ItemStack.EMPTY
//            run findWand@ {
//                for (i in 0..8) {
//                    val stack0 = player.inventory.getItem(i)
//                    if (!stack0.isEmpty && stack0.item is IWandLike) {
//                        val uuidW = stack0.get(ModDataComponents.WAND_INVARIABLE)?.uuid
//                        if (uuidW == data.uuid) {
//                            stack = stack0
//                            return@findWand
//                        }
//                    }
//                }
//                val stack1 = player.getItemBySlot(EquipmentSlot.OFFHAND)
//                if (!stack1.isEmpty && stack1.item is IWandLike && stack1.get(ModDataComponents.WAND_INVARIABLE)?.uuid == data.uuid) {
//                    stack = stack1
//                    return@findWand
//                }
//            }

            val w = CNEvents.gatherWandsTracking(player).wands().filter { stack ->
                    val i = stack.get(ModDataComponents.WAND_INVARIABLE)
                    i != null && i.uuid == data.uuid
                }

            if (w.size > 1) CypherNexus.debugWand(Level.ERROR) { "duplicate uuid [${data.uuid}] $w" }
            stack = w.first()

            if (!stack.isEmpty) {
                // TODO check data authentic
                IWandLike.editItemWand(stack, data.cyphers)
                player.getData(WAND_DATA_MAP).updateWandStats(stack, stack.item as IWandLike, player.level())
                PacketDistributor.sendToPlayer(player as ServerPlayer, data.confirm())
            }

        }.exceptionally {
            CypherNexus.LOGGER.warn(it.message)
            return@exceptionally null
        }
    }


    fun performWandModule(data: ServerboundPerformWandModule, context: IPayloadContext) {
        CypherNexus.debugNetwork { "server receive package -> performWandModule: \n$data" }
        context.enqueueWork {
            val player = context.player()
            val instance = player.getData(WAND_DATA_MAP)[data.uuid]!!
            val stack = player.inventory.getItem(data.wandSlot)

            if (!stack.isEmpty && stack.item is IWandLike) {
                instance.module(data.module)
                    ?. perform(player.level(), player, stack, instance, stack.item as IWandLike)
                    ?: CypherNexus.debugNetwork(Level.ERROR) { "module ${data.module} is missing on $instance" }
            } else {
                CypherNexus.debugNetwork(Level.ERROR) { "didn't find wand in slot ${data.wandSlot}" }
            }
        }.exceptionally {
            CypherNexus.LOGGER.warn(it.message)
            return@exceptionally null
        }
    }
}