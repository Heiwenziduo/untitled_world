package com.github.nahnullscience.cypher_nexus.client.event

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.client.network.ClientWandModuleStateManager
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered

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
            if (!IWandLike.validateItemWand(wand)) return
            val instance = (wand.item as IWandLike).itemWandInstance(player.level(), player, wand) ?: return
            val module = instance.module(WandModuleTypes.PRIMARY) ?: return

            // cancel block-breaking effects
            if (module.takeoverVanillaInput) {
                event.setCanceled(true)
                event.setSwingHand(false)
            }
        }
    }


    @SubscribeEvent
    fun moduleKeysTracking(event: ClientTickEvent.Pre) {
        val mc = Minecraft.getInstance()
        if (mc.player != null &&
            mc.level != null &&
            mc.screen == null &&
            mc.overlay == null) {
            // in the game, and not opening a screen
            val player = mc.player!!

            var secondaryIsHandled = false
            for ((i, hand) in InteractionHand.entries.withIndex()) {

                // handle left click <-> PrimaryModule only in MainHand
                if (hand == InteractionHand.MAIN_HAND) {
                    val primaryIsPerforming = run {
                        val stack = player.getItemInHand(hand)
                        if (!IWandLike.validateItemWand(stack)) return@run false
                        val instance = (stack.item as IWandLike).itemWandInstance(player.level(), player, stack) ?: return@run false
                        val module = instance.module(WandModuleTypes.PRIMARY) ?: return@run false

                        if (mc.options.keyAttack.isDown) {
                            ClientWandModuleStateManager.startModule(WandModuleTypes.PRIMARY, module)
                            if (module.takeoverVanillaInput) {
                                // consume all to prevent further process,
                                // this also naturally prevent wand in another hand to function the same module.
                                // however, Minecraft#continueAttack should be handled one more step, see above
                                while (mc.options.keyAttack.consumeClick()) { }
                            }
                            return@run true
                        } else {
                            // handled here, since module is present
                            ClientWandModuleStateManager.endModule(WandModuleTypes.PRIMARY, module)
                            return@run true
                        }
                    }
                    if (!primaryIsPerforming) {
                        ClientWandModuleStateManager.endModule(WandModuleTypes.PRIMARY, null)
                    }
                }


                val stack = player.getItemInHand(hand)
                if (!IWandLike.validateItemWand(stack)) continue
                val wand = stack.item as IWandLike
                val instance = wand.itemWandInstance(player.level(), player, stack) ?: continue
                // handle right click <-> SecondaryModule
                // TODO right click logic needs to be polished
                run {
                    val module = instance.module(WandModuleTypes.SECONDARY) ?: return@run

                    if (mc.options.keyUse.isDown) {
                        ClientWandModuleStateManager.startModule(WandModuleTypes.SECONDARY, module)
                        if (module.takeoverVanillaInput) {
                            while (mc.options.keyAttack.consumeClick()) { }
                        }
                    } else {
                        ClientWandModuleStateManager.endModule(WandModuleTypes.SECONDARY, module)
                    }
                }
            }
        }
    }
}