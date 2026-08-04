package com.github.nahnullscience.cypher_nexus.client.particle

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.AbstractCypherRenderer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.tick.EntityTickEvent
import net.neoforged.neoforge.event.tick.LevelTickEvent

/**
 * the idea is to
 * centralize logic, put visual logic inside renderer, leave behavior logic in common part of a cypher entity
 * */
@EventBusSubscriber(modid = CypherNexus.MOD_ID, value = [Dist.CLIENT])
object CypherTrailParticleManager {

//    private val renderers: Reference2ReferenceOpenHashMap<EntityType<*>, EntityRenderer<*, *>> = Reference2ReferenceOpenHashMap(128)
//    @SubscribeEvent(priority = EventPriority.LOWEST) // on mod specific event bus
//    private fun getRenderers(event: AddLayers) {
//        // it seems net.neoforged.neoforge.client.event.EntityRenderersEvent$AddLayers renderers is untouchable
//        //
//        renderers.clear()
//        event.entityTypes.forEach { type ->
//            val renderer = event.getRenderer(type) ?: return@forEach
//            renderers[type] = renderer
//        }
//    }

    @SubscribeEvent
    private fun makeSureGroupInEngine(event: LevelTickEvent.Pre) {
        // for we don't know the timing engine calls #clear
        // loop this and make sure our instance is not stale
        if (event.level.isClientSide) CypherTrailParticleGroup.updateInstance()
    }

    @SubscribeEvent
    private fun updateCERenderStateAndAddTrailParticle(event: EntityTickEvent.Post) {
        if (event.entity.level().isClientSide) event.entity.let { ce ->
            if (ce is ICypherEntity) {
                getCERenderer(ce)?.
                clientTickPost(level!!, ce, ce.x, ce.y, ce.z, ce.xo, ce.yo, ce.zo)
            }
        }
    }

    val entityRenderDispatcher get() = Minecraft.getInstance().entityRenderDispatcher
    val level get() = Minecraft.getInstance().level


    fun <CE> getCERenderer(entity: CE): AbstractCypherRenderer<CE, *>?
            where CE : Entity, CE : ICypherEntity {
        return entityRenderDispatcher.getRenderer(entity) as AbstractCypherRenderer<CE, *>?
    }

//    private val trailingCE: Int2ReferenceOpenHashMap<CERenderState> = Int2ReferenceOpenHashMap(256)
//    data class CERenderState(
//        val x: Double,
//        val y: Double,
//        val z: Double,
//    ) {}
}