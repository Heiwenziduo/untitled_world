package com.github.nahnullscience.cypher_nexus.client.renderer

import com.github.nahnullscience.cypher_nexus.client.cypher.CypherRenderState
import com.github.nahnullscience.cypher_nexus.content.entity.Arrow
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture

class ArrowCypherRenderer (
    context: EntityRendererProvider.Context
) : AbstractCypherRenderer<Arrow>(context) {
    override fun submit(
        state: CypherRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
        poseStack.pushPose()
//        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0f))
//        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot))
//        submitNodeCollector.submitModel<S?>(
//            this.model,
//            state,
//            poseStack,
//            this.getTextureLocation(state),
//            state.lightCoords,
//            OverlayTexture.NO_OVERLAY,
//            state.outlineColor,
//            null
//        )
        poseStack.popPose()
        super.submit(state, poseStack, submitNodeCollector, camera)
    }
}