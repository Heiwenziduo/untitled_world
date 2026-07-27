package com.github.nahnullscience.cypher_nexus.client.renderer.cypher

import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.ParticleProjectileRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.StandingSignRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.world.level.block.PlainSignBlock
import net.minecraft.world.level.block.state.properties.WoodType

open class SimpleParticleProjectileRenderer  <CE : AbstractDedicatedCypherProjectile> (
    context: Context
) : AbstractCypherRenderer<CE, ParticleProjectileRenderState>(context) {

    override fun createRenderState() = ParticleProjectileRenderState()

    override fun extractRenderState(entity: CE, state: ParticleProjectileRenderState, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
    }

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

//        submitNodeCollector.submitModel(
//            model,
//            Unit.INSTANCE,
//            poseStack,
//            NORMAL_ARROW_LOCATION,
//            state.lightCoords,
//            OverlayTexture.NO_OVERLAY,
//            state.outlineColor,
//            null
//        )

//        Minecraft.getInstance().level?.let { level ->
//            level.addParticle(ParticleTypes.BUBBLE, state.x, state.y, state.z, 1.0, 1.0, 1.0)
//            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, state.x, state.y, state.z, 0.0, 0.0, 0.0)
//        }

//        addCypherTrailParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, state.x, state.y, state.z, 0.0, 0.0, 0.0) {
//            setColor(0.5f, 1.0f, 0.6f)
//            setAlpha(0.6f)
//        }

        super.submit(state, poseStack, submitNodeCollector, camera)
    }
}