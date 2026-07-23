package com.github.nahnullscience.cypher_nexus.client.renderer.cypher

import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.ParticleProjectileRenderState
import com.github.nahnullscience.cypher_nexus.client.renderer.utility.TintedVertexConsumer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.client.renderer.state.level.CameraRenderState

class SimpleParticleProjectileRenderer  <CY : AbstractDedicatedCypherProjectile> (
    context: Context
) : AbstractCypherRenderer<CY, ParticleProjectileRenderState>(context) {

    override fun createRenderState() = ParticleProjectileRenderState()

    override fun submit(
        state: ParticleProjectileRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
//        val tintedCollector = SubmitNodeCollector { renderType ->
//            val originalConsumer = submitNodeCollector.getBuffer(renderType)
//
//            // Example: Ice-Blue Tint (R: 30%, G: 80%, B: 100%)
//            TintedVertexConsumer(
//                delegate = originalConsumer,
//                redFilter = 0.3f,
//                greenFilter = 0.8f,
//                blueFilter = 1.0f,
//                alphaFilter = 0.9f // Slightly translucent!
//            )
//        }

        super.submit(state, poseStack, submitNodeCollector, camera)
    }
}