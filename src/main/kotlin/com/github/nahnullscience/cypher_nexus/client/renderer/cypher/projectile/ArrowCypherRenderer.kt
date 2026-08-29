package com.github.nahnullscience.cypher_nexus.client.renderer.cypher.projectile

import com.github.nahnullscience.cypher_nexus.client.util.addCypherTrailParticle
import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.AbstractCypherRenderer
import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.projectile.ArrowCypherRenderState
import com.github.nahnullscience.cypher_nexus.content.entity.projectile.Arrow
import com.github.nahnullscience.cypher_nexus.utility.forEachBetween
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.model.`object`.projectile.ArrowModel
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.client.renderer.entity.TippableArrowRenderer.NORMAL_ARROW_LOCATION
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.particles.ParticleTypes

class ArrowCypherRenderer (
    context: Context
) : AbstractCypherRenderer<Arrow, ArrowCypherRenderState>(context) {
    val model = ArrowModel(context.bakeLayer(ModelLayers.ARROW))

    override fun submit(
        state: ArrowCypherRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
        poseStack.pushPose()
        poseStack.scaleByEffectRadius(state)
        poseStack.rotateToSpeed(state)
        poseStack.translate(-0.125f, 0f, 0f)
        submitNodeCollector.submitModel(
            model,
            state,
            poseStack,
            NORMAL_ARROW_LOCATION,
            state.lightCoords,
            OverlayTexture.NO_OVERLAY,
            state.outlineColor,
            null
        )
        poseStack.popPose()
        super.submit(state, poseStack, submitNodeCollector, camera)
    }

    override fun addTrailParticles(
        level: ClientLevel,
        ce: Arrow,
        x: Double, y: Double, z: Double,
        xo: Double, yo: Double, zo: Double
    ) {
        val speed = ce.deltaMovement
        if (ce.tickCount and 1 == 1 && ce.tickStartSpeedSqr >= 1.5)
        forEachBetween(
            xo, yo, zo,
            x, y, z,
            1
        ) { step, x, y, z ->
            addCypherTrailParticle(
                ce,
                ParticleTypes.CRIT,
                x, y, z,
                -speed.x * 0.25,
                -speed.y * 0.25,
                -speed.z * 0.25
            )
        }
    }

    override fun createRenderState() = ArrowCypherRenderState()
    override fun extractRenderState(entity: Arrow, state: ArrowCypherRenderState, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
        state.shake = entity.shakeTime - partialTicks
    }
}
