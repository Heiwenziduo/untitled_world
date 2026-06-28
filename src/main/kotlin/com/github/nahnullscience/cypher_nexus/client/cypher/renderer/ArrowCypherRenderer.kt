package com.github.nahnullscience.cypher_nexus.client.cypher.renderer

import com.github.nahnullscience.cypher_nexus.client.cypher.state.ArrowCypherRenderState
import com.github.nahnullscience.cypher_nexus.content.entity.Arrow
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.model.`object`.projectile.ArrowModel
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.TippableArrowRenderer.NORMAL_ARROW_LOCATION
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture

class ArrowCypherRenderer (
    context: EntityRendererProvider.Context
) : AbstractCypherRenderer<Arrow, ArrowCypherRenderState>(context) {
    val model = ArrowModel(context.bakeLayer(ModelLayers.ARROW))

    override fun submit(
        state: ArrowCypherRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
        poseStack.pushPose()
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0f))
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot))
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

    override fun createRenderState() = ArrowCypherRenderState()
    override fun extractRenderState(entity: Arrow, state: ArrowCypherRenderState, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
        state.xRot = entity.getXRot(partialTicks)
        state.yRot = entity.getYRot(partialTicks)
        state.shake = entity.shakeTime - partialTicks
    }
}
