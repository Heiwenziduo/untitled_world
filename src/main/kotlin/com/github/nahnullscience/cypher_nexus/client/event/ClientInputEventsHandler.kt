package com.github.nahnullscience.cypher_nexus.client.event

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.client.network.ClientInputModuleStateUpdater
import com.github.nahnullscience.cypher_nexus.client.network.ClientInputModuleStateUpdater.endModule
import com.github.nahnullscience.cypher_nexus.client.network.ClientInputModuleStateUpdater.startModule
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes
import com.github.nahnullscience.cypher_nexus.mechanic.wand.AbstractItemWand.Companion.wandInstanceOrNull
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
object ClientInputEventsHandler {
    val mc get() = Minecraft.getInstance()

    @SubscribeEvent(priority = EventPriority.LOW)
    private fun handleMouseInput(event: InteractionKeyMappingTriggered) {
        val player = mc.player ?: return

        // if player is holding a wand and the wand has some modules by chance
        // the module will take over the default input handling
        if (event.isAttack) {

            val wand = player.getItemInHand(InteractionHand.MAIN_HAND)
            val instance = wand.wandInstanceOrNull(player) ?: return
            val module = instance.getModule(WandModuleTypes.PRIMARY_MODULE) ?: return

            // cancel block-breaking effects
            if (module.consumeVanillaInput) {
                event.setCanceled(true)
                event.setSwingHand(false)
            }
        }
    }


    @SubscribeEvent
    private fun moduleKeysTracking(event: ClientTickEvent.Pre) {
        if (mc.player != null && mc.level != null) {

            // if in screen, stop input-modules
            if (mc.screen != null || mc.overlay != null) {
                ClientInputModuleStateUpdater.endAllInputModule()
                return
            }

            // in the game, and not opening a screen
            val player = mc.player!!

            var primaryIsPerforming = false
            var secondaryIsPerforming = false
            for (hand in InteractionHand.entries) {
                val stack = player.getItemInHand(hand)
                val instance = stack.wandInstanceOrNull(player)

                // main-hand-wand has Primary module && mouse button down -> Yes, otherwise No
                if (hand == InteractionHand.MAIN_HAND) run Primary@ {
                    instance ?: return@Primary false
                    val module = instance.getModule(WandModuleTypes.PRIMARY_MODULE) ?: return@Primary false

                    if (mc.options.keyAttack.isDown) {
                        if (module.consumeVanillaInput) {
                            // consume all to prevent further process,
                            // this also naturally prevent wand in another hand to function the same module.
                            // however, Minecraft#continueAttack should be handled one more step, see above
                            while (mc.options.keyAttack.consumeClick()) { //
                            }
                        }
                        return@Primary true
                    } else {
                        return@Primary false
                    }
                }.also { primaryIsPerforming = it }

                // TODO when point something and not consumeVanillaInput, don't start

                instance ?: continue
                // wand in any hand has Secondary module && mouse button down -> Yes, otherwise No
                run Secondary@ {
                    val module = instance.getModule(WandModuleTypes.SECONDARY_MODULE) ?: return@Secondary false

                    if (mc.options.keyUse.isDown) {
                        if (module.consumeVanillaInput) {
                            while (mc.options.keyAttack.consumeClick()) {
                                //
                            }
                        }
                        return@Secondary true
                    } else {
                        return@Secondary false
                    }
                }.also {
                    secondaryIsPerforming = it
                    if (it) break
                }
            }

            if (primaryIsPerforming) startModule(WandModuleTypes.PRIMARY_MODULE)
            else endModule(WandModuleTypes.PRIMARY_MODULE)

            if (secondaryIsPerforming) startModule(WandModuleTypes.SECONDARY_MODULE)
            else endModule(WandModuleTypes.SECONDARY_MODULE)
        }
    }
}