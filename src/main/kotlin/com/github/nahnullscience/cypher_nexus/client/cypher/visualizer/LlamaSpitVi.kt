package com.github.nahnullscience.cypher_nexus.client.cypher.visualizer

import com.github.nahnullscience.cypher_nexus.client.cypher.ICypherVisualizer
import com.github.nahnullscience.cypher_nexus.content.cypher.projectile.LlamaSpitCypher
import com.github.nahnullscience.cypher_nexus.content.entity.CypherProjectile
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.block.BlockRenderDispatcher
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.util.Mth

object LlamaSpitVi : ICypherVisualizer {
    override fun cypher() = LlamaSpitCypher

    override fun render(
        projectile: CypherProjectile,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        itemRenderer: ItemRenderer,
        blockRenderer: BlockRenderDispatcher,
        entityRenderDispatcher: EntityRenderDispatcher
    ) {
        poseStack.pushPose()
        poseStack.translate(0.0f, 0.15f, 0.0f)
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, projectile.yRotO, projectile.yRot) - 90.0f))
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, projectile.xRotO, projectile.xRot)))

        // FIXME
        // Q: How to dynamically use model?

//        this.model.setupAnim(projectile, partialTick, 0.0f, -0.1f, 0.0f, 0.0f)
//        val vertexconsumer: VertexConsumer =
//            bufferSource.getBuffer(this.model.renderType(LlamaSpitRenderer.LLAMA_SPIT_LOCATION))
//        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY)

        poseStack.popPose()
    }
}