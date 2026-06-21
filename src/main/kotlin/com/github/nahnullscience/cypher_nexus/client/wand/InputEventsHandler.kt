package com.github.nahnullscience.cypher_nexus.client.wand

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.ModuleCategory
import com.github.nahnullscience.cypher_nexus.network.server.ServerboundPerformWandModule
import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered
import net.neoforged.neoforge.client.network.ClientPacketDistributor

@EventBusSubscriber(modid = CypherNexus.MOD_ID, value = [Dist.CLIENT])
object InputEventsHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    fun handleMouseInput(event: InteractionKeyMappingTriggered) {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return

        // if player is holding a wand and the wand has some modules by chance
        // the module will take over the default input handling
        if (event.isAttack) {

            val wand = player.getItemInHand(InteractionHand.MAIN_HAND)
            if (wand.isEmpty || wand.item !is IWandLike) return
            val instance = (wand.item as IWandLike).itemWandInstance(player.level(), player, wand) ?: return
            val module = instance.module(ModuleCategory.PRIMARY) ?: return

            if (module.takeoverInput) {
                event.setCanceled(true)
            }

            ClientPacketDistributor.sendToServer(
                ServerboundPerformWandModule(
                    instance.uuid,
                    module.category,
                    player.inventory.selectedSlot
                )
            )
            module.perform(player.level(), player, wand, instance, wand.item as IWandLike)
        } else if (event.isUseItem) {

        }
    }
}