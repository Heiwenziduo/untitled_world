package com.github.nahnullscience.cypher_nexus.client.renderer.cypher

import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.LlamaSpitCypherRenderState
import com.github.nahnullscience.cypher_nexus.content.entity.projectiles.LlamaSpit
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.model.animal.llama.LlamaSpitModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.client.renderer.entity.LlamaSpitRenderer
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture

class LlamaSpitCypherRenderer (
    context: Context
) : AbstractCypherRenderer<LlamaSpit, LlamaSpitCypherRenderState>(context) {

    val model = LlamaSpitModel(context.bakeLayer(ModelLayers.LLAMA_SPIT))
    override fun submit(
        state: LlamaSpitCypherRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
        poseStack.pushPose()
        poseStack.translate(0.0f, 0.15f, 0.0f)
        poseStack.scaleByEffectRadius(state)
        poseStack.rotateToSpeed(state)
//        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0f))
//        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot))
        submitNodeCollector.submitModel(
            model,
            state,
            poseStack,
            LlamaSpitRenderer.LLAMA_SPIT_LOCATION,
            state.lightCoords,
            OverlayTexture.NO_OVERLAY,
            state.outlineColor,
            null
        )
        poseStack.popPose()
        super.submit(state, poseStack, submitNodeCollector, camera)
    }

    override fun createRenderState() = LlamaSpitCypherRenderState()
    override fun extractRenderState(entity: LlamaSpit, state: LlamaSpitCypherRenderState, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
    }
}