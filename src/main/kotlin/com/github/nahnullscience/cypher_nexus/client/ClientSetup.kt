package com.github.nahnullscience.cypher_nexus.client

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.client.gui.WandDataOverlay
import com.github.nahnullscience.cypher_nexus.client.cypher.renderer.ArrowCypherRenderer
import com.github.nahnullscience.cypher_nexus.client.cypher.renderer.LlamaSpitCypherRenderer
import com.github.nahnullscience.cypher_nexus.client.cypher.renderer.SimpleItemProjectileRenderer
import com.github.nahnullscience.cypher_nexus.client.cypher.renderer.SimpleParticleProjectileRenderer
import com.github.nahnullscience.cypher_nexus.client.cypher.renderer.SimpleSummonerRenderer
import com.github.nahnullscience.cypher_nexus.init.ModEntities
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.ItemSupplier
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent
import net.neoforged.neoforge.client.gui.VanillaGuiLayers.CONTEXTUAL_INFO_BAR
import java.util.function.Supplier

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
//        CypherNexus.LOGGER.info("HELLO FROM CLIENT SETUP")
//        CypherNexus.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().user.name)

//        // only on physical client
//        // sided setup is the last step of 4-step neo lifecycle, here registry is fully prepared
//        CypherVisualizerRegistry.init()
    }

//    @SubscribeEvent
//    private fun registerClientPayloadHandler(event: RegisterClientPayloadHandlersEvent) {
//
//    }

    // ===================== entity renderer ================================
    @SubscribeEvent
    private fun registerEntityRenderers(event: RegisterRenderers) {

        //////////////////////////////////////////////////////////////////////////////
        // projectile
        //////////////////////////////////////////////////////////////////////////////
        event.registerEntityRenderer(ModEntities.CYPHER_ARROW.get(), ::ArrowCypherRenderer)
        event.registerEntityRenderer(ModEntities.CYPHER_LLAMA_SPIT, ::LlamaSpitCypherRenderer)

        event.registerItemProjectile(ModEntities.CYPHER_SNOWBALL)
        event.registerItemProjectile(ModEntities.CYPHER_ENDER_RECALL)
        event.registerItemProjectile(ModEntities.CYPHER_ENDER_TELEPORTATION)
        event.registerItemProjectile(ModEntities.CYPHER_SPAWN_EGG)

        event.registerParticleProjectile(ModEntities.CYPHER_BUBBLE_COLUMN)

        //////////////////////////////////////////////////////////////////////////////
        // static
        //////////////////////////////////////////////////////////////////////////////
        event.registerEntityRenderer(ModEntities.CYPHER_EXPLOSION, ::SimpleSummonerRenderer)
    }

//    @SubscribeEvent
//    fun registerRenderStateModifiers(event: RegisterRenderStateModifiersEvent) {
//    }

    @SubscribeEvent
    private fun registerLayerDefinitions(event: EntityRenderersEvent.RegisterLayerDefinitions) {

    }

    @SubscribeEvent
    private fun registerGuiLayersEvent(event: RegisterGuiLayersEvent) {
        event.registerBelow(CONTEXTUAL_INFO_BAR, CypherNexus.modResource("wand_data"), WandDataOverlay)
    }
}

private fun <CY> RegisterRenderers.registerItemProjectile (
    cypherEntity: Supplier<out EntityType<out CY>>,
) where CY : AbstractCypherProjectile, CY : ItemSupplier
        = registerEntityRenderer(cypherEntity.get()) { context -> SimpleItemProjectileRenderer(context) }

private fun <CY> RegisterRenderers.registerParticleProjectile (
    cypherEntity: Supplier<out EntityType<out CY>>,
) where CY : AbstractCypherProjectile
        = registerEntityRenderer(cypherEntity.get()) { context -> SimpleParticleProjectileRenderer(context) }

private fun <T : AbstractCypherProjectile> RegisterRenderers.registerEntityRenderer(
    cypherEntity: Supplier<out EntityType<out T>>,
    factory: EntityRendererProvider<T>
) = registerEntityRenderer(cypherEntity.get(), factory)