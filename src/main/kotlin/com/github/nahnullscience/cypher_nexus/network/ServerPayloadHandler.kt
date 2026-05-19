package com.github.nahnullscience.cypher_nexus.network

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.item.BasicWandItem
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.network.server.ServerboundEditWandCyphers
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.network.handling.IPayloadContext

/** client counterpart resides in com.github.heiwenziduo.cypher_nexus.client.network */
object ServerPayloadHandler {
    fun editWandCyphers(data: ServerboundEditWandCyphers, context: IPayloadContext) {
        println("server receive package -> editWandCyphers: \n$data")


        context.enqueueWork {
            val player = context.player()
            var stack: ItemStack = ItemStack.EMPTY
            run findWand@{
                for (i in 0..8) {
                    val stack0 = player.inventory.getItem(i)
                    if (!stack0.isEmpty && stack0.item is IWandLike) {
                        val uuidW = stack0.get(ModDataComponents.WAND_INVARIABLE)?.chunkU?.uuid
                        if (uuidW == data.uuid) {
                            stack = stack0
                            return@findWand
                        }
                    }
                }
                val stack1 = player.getItemBySlot(EquipmentSlot.OFFHAND)
                if (!stack1.isEmpty && stack1.item is IWandLike && stack1.get(ModDataComponents.WAND_INVARIABLE)?.chunkU?.uuid == data.uuid) {
                    stack = stack1
                    return@findWand
                }
                // TODO gatherWands event
            }

            if (!stack.isEmpty) {
                BasicWandItem.Companion.editWand(stack, data.cyphers)
                BasicWandItem.Companion.resetIndex(stack)
            }

        }.exceptionally {
            CypherNexus.LOGGER.warn(it.message)
            return@exceptionally null
        }
    }
}