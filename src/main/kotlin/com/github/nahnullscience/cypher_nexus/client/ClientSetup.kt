package com.github.nahnullscience.cypher_nexus.client

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.client.devtools.WebServiceManager
import com.github.nahnullscience.cypher_nexus.client.gui.WandDataOverlay
import com.github.nahnullscience.cypher_nexus.client.particle.CypherTrailParticleGroup
import com.github.nahnullscience.cypher_nexus.client.particle.CypherTrailParticleGroup.Companion.CYPHER_TRAIL_RENDER_TYPE
import com.github.nahnullscience.cypher_nexus.client.particle.DistanceInvokeTrail
import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.*
import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.projectile.ArrowCypherRenderer
import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.projectile.BubbleColumnCypherRenderer
import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.projectile.FireworkRocketCypherRenderer
import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.projectile.LlamaSpitCypherRenderer
import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.projectile.SmokeBombCypherRenderer
import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.utility.DistanceDeliverCypherRenderer
import com.github.nahnullscience.cypher_nexus.init.ModEntities
import com.github.nahnullscience.cypher_nexus.init.ModParticleTypes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.commands.Commands
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.*
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers
import net.neoforged.neoforge.client.gui.ConfigurationScreen
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import net.neoforged.neoforge.client.gui.VanillaGuiLayers.CONTEXTUAL_INFO_BAR
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent
import java.util.function.Supplier


@EventBusSubscriber(modid = CypherNexus.MOD_ID, value = [Dist.CLIENT])
object ClientSetup {
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


        val container = ModLoadingContext.get().activeContainer
        // register default config screen
        container.registerExtensionPoint(
            IConfigScreenFactory::class.java,
            IConfigScreenFactory { container, parent ->
                ConfigurationScreen(container, parent)
            }
        )
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
        event.registerEntityRenderer(ModEntities.CYPHER_ARROW, ::ArrowCypherRenderer)
        event.registerEntityRenderer(ModEntities.CYPHER_LLAMA_SPIT, ::LlamaSpitCypherRenderer)

        event.registerItemProjectile(ModEntities.CYPHER_SNOWBALL, Items.SNOWBALL)
        event.registerItemProjectile(ModEntities.CYPHER_ENDER_RECALL, Items.ENDER_PEARL)
        event.registerItemProjectile(ModEntities.CYPHER_ENDER_TELEPORTATION, Items.ENDER_PEARL)
        event.registerItemProjectile(ModEntities.CYPHER_SPAWN_EGG, Items.EGG)

        event.registerEntityRenderer(ModEntities.CYPHER_BUBBLE_COLUMN, ::BubbleColumnCypherRenderer)
        event.registerParticleProjectile(ModEntities.CYPHER_DRILLING_BOLT)
        event.registerParticleProjectile(ModEntities.CYPHER_DRILLING_BLAST)
        event.registerEntityRenderer(ModEntities.CYPHER_SMOKE_BOMB, ::SmokeBombCypherRenderer)
        event.registerEntityRenderer(ModEntities.CYPHER_FIREWORK_ROCKET, ::FireworkRocketCypherRenderer)
        event.registerEntityRenderer(ModEntities.CYPHER_RANDOM_FIREWORK_ROCKET, ::FireworkRocketCypherRenderer)

        //////////////////////////////////////////////////////////////////////////////
        // static
        //////////////////////////////////////////////////////////////////////////////
        event.registerEntityRenderer(ModEntities.CYPHER_EXPLOSION, ::InvisibleRenderer)
        event.registerEntityRenderer(ModEntities.CYPHER_LIGHTING, ::InvisibleRenderer)

        //////////////////////////////////////////////////////////////////////////////
        // utility
        //////////////////////////////////////////////////////////////////////////////
        event.registerEntityRenderer(ModEntities.CYPHER_DISTANCE_DELIVERER, ::DistanceDeliverCypherRenderer)

    }

    @SubscribeEvent
    private fun registerRenderStateModifiers(event: RegisterRenderStateModifiersEvent) {

    }

    @SubscribeEvent
    private fun registerLayerDefinitions(event: EntityRenderersEvent.RegisterLayerDefinitions) {

    }

    @SubscribeEvent
    private fun registerParticleProviders(event: RegisterParticleGroupsEvent) {
        // for particleEngine #particleRenderOrder and #particleGroupFactories
        event.register(CYPHER_TRAIL_RENDER_TYPE, ::CypherTrailParticleGroup)
    }

    @SubscribeEvent
    private fun registerParticleProviders(event: RegisterParticleProvidersEvent) {
        // There are multiple ways to register providers, all differing in the functional type they provide in the
        // second parameter. For example, #registerSpriteSet represents a Function<SpriteSet, ParticleProvider<?>>:
        // #registerSpecial, on the other hand, maps to a ParticleProvider<?>.
        // This should be used if the sprite is not obtained from the particle description.
        event.registerSpriteSet(ModParticleTypes.DISTANCE_INVOKE_TRAIL.get()) { spriteSet -> DistanceInvokeTrail.TrailProvider(spriteSet) }
    }

    @SubscribeEvent
    private fun registerGuiLayersEvent(event: RegisterGuiLayersEvent) {
        event.registerBelow(CONTEXTUAL_INFO_BAR, CypherNexus.modResource("wand_data"), WandDataOverlay)
    }


    @SubscribeEvent
    private fun registerClientCommands(event: RegisterClientCommandsEvent) {
        val dispatcher = event.dispatcher
        val buildContext = event.buildContext

        dispatcher.register(
            Commands.literal("cypher_nexus")
                .then(WebServiceManager.command)
        )
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun <CE> RegisterRenderers.registerItemProjectile (
        ceSupplier: Supplier<out EntityType<out CE>>,
        item: Item
    ) where CE : AbstractDedicatedCypherProjectile {
        registerEntityRenderer(ceSupplier.get()) { context ->
            SimpleItemProjectileRenderer(context, item)
        }
    }

    private fun <CE> RegisterRenderers.registerParticleProjectile (
        ceSupplier: Supplier<out EntityType<out CE>>,
    ) where CE : AbstractDedicatedCypherProjectile {
        registerEntityRenderer(ceSupplier.get()) { context ->
            SimpleParticleProjectileRenderer(context)
        }
    }

    private fun <CE> RegisterRenderers.registerEntityRenderer(
        ceSupplier: Supplier<out EntityType<out CE>>,
        factory: EntityRendererProvider<CE>
    ) where CE : Entity, CE : ICypherEntity {
        registerEntityRenderer(ceSupplier.get(), factory)
    }
}
