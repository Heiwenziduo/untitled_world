package com.github.nahnullscience.cypher_nexus.client

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.client.cypher.CypherVisualizerRegistry
import com.github.nahnullscience.cypher_nexus.client.cypher.CypherProjectileRenderer
import com.github.nahnullscience.cypher_nexus.client.gui.WandDataOverlay
import com.github.nahnullscience.cypher_nexus.init.ModEntities
import net.minecraft.client.Minecraft
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent

@EventBusSubscriber(modid = CypherNexus.MOD_ID, value = [Dist.CLIENT])
object ClientSetup {
    // TODO: separate logical and physical clients?

    /**
     * This is used for initializing client specific things such as renderers and keymaps.
     * Fired on the mod specific event bus.
     *
     * Sided setup event's dist seems to be auto-detected, "...The sided setup is fired:
     * FMLClientSetupEvent if on a physical client, and FMLDedicatedServerSetupEvent if on a physical server..."
     */
    @SubscribeEvent
    private fun onClientStarting(event: FMLClientSetupEvent) {
        CypherNexus.LOGGER.info("HELLO FROM CLIENT SETUP")
        CypherNexus.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().user.name)
        // only on physical client
        // sided setup is the last step of 4-step neo lifecycle, here registry is fully prepared
        CypherVisualizerRegistry.init()
    }

//    @SubscribeEvent
//    private fun registerClientPayloadHandler(event: RegisterClientPayloadHandlersEvent) {
//
//    }

    // ===================== entity renderer ================================
    @SubscribeEvent
    private fun registerEntityRenderers(event: RegisterRenderers) {
        event.registerEntityRenderer(ModEntities.CYPHER_PROJECTILE.get()) { context -> CypherProjectileRenderer(context) }
    }

//    @SubscribeEvent
//    fun registerRenderStateModifiers(event: RegisterRenderStateModifiersEvent) {
//    }

    @SubscribeEvent
    private fun registerLayerDefinitions(event: EntityRenderersEvent.RegisterLayerDefinitions) {

    }

    @SubscribeEvent
    private fun registerGuiLayersEvent(event: RegisterGuiLayersEvent) {
        event.registerAboveAll(CypherNexus.modResource("wand_data"), WandDataOverlay)
    }
}