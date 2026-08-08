package com.github.nahnullscience.cypher_nexus.client.event

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.client.network.ClientInputModuleStateUpdater
import com.github.nahnullscience.cypher_nexus.client.network.ClientInputModuleStateUpdater.endModule
import com.github.nahnullscience.cypher_nexus.client.network.ClientInputModuleStateUpdater.startModule
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IItemWand.Companion.wandInstanceOrNull
import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered

@EventBusSubscriber(modid = CypherNexus.MOD_ID, value = [Dist.CLIENT])
object ClientInputEventsHandler {
    private val mc get() = Minecraft.getInstance()

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
        val level = mc.level
        val player = mc.player
        if (player != null && level != null) {

            // if in screen, stop input-modules
            if (mc.screen != null || mc.overlay != null || player.isSpectator) {
                ClientInputModuleStateUpdater.endAllInputModule()
                return
            }

            // in the game, and not opening a screen

            var primaryIsPerforming = false
            var secondaryIsPerforming = false
            for (hand in InteractionHand.entries) {
                val stack = player.getItemInHand(hand)
                val instance = stack.wandInstanceOrNull(player)

                // main-hand-wand has Primary module && mouse button down -> Yes, otherwise No
                if (hand == InteractionHand.MAIN_HAND)
                run Primary@ {
                    instance ?: return@Primary false
                    val module = instance.getModule(WandModuleTypes.PRIMARY_MODULE) ?: return@Primary false

                    if (!mc.options.keyAttack.isDown) return@Primary false
                    else if (module.consumeVanillaInput) {
                        // consume all to prevent further process,
                        // this also naturally prevent wand in another hand to function the same module.
                        // however, Minecraft#continueAttack should be handled one more step, see above
                        while (mc.options.keyAttack.consumeClick()) { //
                        }
                        return@Primary true
                    } else {
                        return@Primary true
                    }
                }.also { primaryIsPerforming = it }

                instance ?: continue
                // wand in any hand has Secondary module && mouse button down -> Yes, otherwise No
                run Secondary@ {
                    val module = instance.getModule(WandModuleTypes.SECONDARY_MODULE) ?: return@Secondary false

                    if (!mc.options.keyUse.isDown) return@Secondary false
                    else if (module.consumeVanillaInput) {
                        while (mc.options.keyAttack.consumeClick()) { //
                        }
                        return@Secondary true
                    } else {
                        // when vanilla input matters, don't fire when point at something interactable
                        if (vanillaRightClickTargetCheck(hand)) {
                            secondaryIsPerforming = false
                            break
                        }

                        return@Secondary true
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

    /**
     *
     * @return true if point at something that interactable and the right-click module should not start
     * @see [Minecraft.startUseItem]
     * */
    private fun vanillaRightClickTargetCheck(hand: InteractionHand): Boolean {
        mc.player?.let { player ->
            if (player.isUsingItem) return false // when has been using always ignore
            when (val result = mc.hitResult) {
                is EntityHitResult -> {
                    val entity = result.entity
                    val location = entity.position().vectorTo(result.location)
                    val result = player.interactOn(entity, hand, location)
                    if (result is InteractionResult.Success) return true
                }
                is BlockHitResult -> {
                    mc.gameMode?.performUseItemOn(player, hand, result)?.let {
                        if (it is InteractionResult.Success || it is InteractionResult.Fail) return true
                    }
                }
            }
        }
        return false
    }
}