package com.github.nahnullscience.cypher_nexus.client.renderer.cypher

import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.ParticleProjectileRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.particle.SingleQuadParticle
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.client.renderer.state.level.CameraRenderState

class SimpleParticleProjectileRenderer  <CE : AbstractDedicatedCypherProjectile> (
    context: Context
) : AbstractCypherRenderer<CE, ParticleProjectileRenderState>(context) {

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
//        submitNodeCollector.submitParticleGroup(state.particles)

        super.submit(state, poseStack, submitNodeCollector, camera)
    }

    override fun extractRenderState(entity: CE, state: ParticleProjectileRenderState, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
//        state.particles.add(
//            SingleQuadParticle.Layer.OPAQUE,
//            state.x.toFloat(),
//            state.y.toFloat(),
//            state.z.toFloat(),
//            0f,
//            0f,
//            0f,
//            0f,
//            1f,
//            8f,
//            8f,
//            8f,
//            8f,
//            0xFF_FFCCFF.toInt(),
//            15728640
//        )
    }
}