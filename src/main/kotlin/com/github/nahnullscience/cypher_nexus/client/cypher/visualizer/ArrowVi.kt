package com.github.nahnullscience.cypher_nexus.client.cypher.visualizer

import com.github.nahnullscience.cypher_nexus.client.cypher.ICypherVisualizer
import com.github.nahnullscience.cypher_nexus.content.cypher.projectile.ArrowCypher
import com.github.nahnullscience.cypher_nexus.content.entity.CypherProjectile
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Axis
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.block.BlockRenderDispatcher
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.renderer.entity.TippableArrowRenderer.NORMAL_ARROW_LOCATION
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.util.Mth

object ArrowVi : ICypherVisualizer {
    override fun cypher() = ArrowCypher

    /** {net.minecraft.client.renderer.entity.ArrowRenderer} */
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
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, projectile.yRotO, projectile.yRot) - 90.0f))
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, projectile.xRotO, projectile.xRot)))
        val i = 0
        val f = 0.0f
        val f1 = 0.5f
        val f2 = 0.0f
        val f3 = 0.15625f
        val f4 = 0.0f
        val f5 = 0.15625f
        val f6 = 0.15625f
        val f7 = 0.3125f
        val f8 = 0.05625f
//        val f9: Float = projectile.shakeTime.toFloat() - partialTick
        val f9: Float = 0f - partialTick
        if (f9 > 0.0f) {
            val f10 = -Mth.sin(f9 * 3.0f) * f9
            poseStack.mulPose(Axis.ZP.rotationDegrees(f10))
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(45.0f))
        poseStack.scale(0.05625f, 0.05625f, 0.05625f)
        poseStack.translate(-4.0f, 0.0f, 0.0f)
        val vertexconsumer: VertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(NORMAL_ARROW_LOCATION))
        val `posestack$pose` = poseStack.last()
        this.vertex(`posestack$pose`, vertexconsumer, -7, -2, -2, 0.0f, 0.15625f, -1, 0, 0, packedLight)
        this.vertex(`posestack$pose`, vertexconsumer, -7, -2, 2, 0.15625f, 0.15625f, -1, 0, 0, packedLight)
        this.vertex(`posestack$pose`, vertexconsumer, -7, 2, 2, 0.15625f, 0.3125f, -1, 0, 0, packedLight)
        this.vertex(`posestack$pose`, vertexconsumer, -7, 2, -2, 0.0f, 0.3125f, -1, 0, 0, packedLight)
        this.vertex(`posestack$pose`, vertexconsumer, -7, 2, -2, 0.0f, 0.15625f, 1, 0, 0, packedLight)
        this.vertex(`posestack$pose`, vertexconsumer, -7, 2, 2, 0.15625f, 0.15625f, 1, 0, 0, packedLight)
        this.vertex(`posestack$pose`, vertexconsumer, -7, -2, 2, 0.15625f, 0.3125f, 1, 0, 0, packedLight)
        this.vertex(`posestack$pose`, vertexconsumer, -7, -2, -2, 0.0f, 0.3125f, 1, 0, 0, packedLight)

        for (j in 0..3) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f))
            this.vertex(`posestack$pose`, vertexconsumer, -8, -2, 0, 0.0f, 0.0f, 0, 1, 0, packedLight)
            this.vertex(`posestack$pose`, vertexconsumer, 8, -2, 0, 0.5f, 0.0f, 0, 1, 0, packedLight)
            this.vertex(`posestack$pose`, vertexconsumer, 8, 2, 0, 0.5f, 0.15625f, 0, 1, 0, packedLight)
            this.vertex(`posestack$pose`, vertexconsumer, -8, 2, 0, 0.0f, 0.15625f, 0, 1, 0, packedLight)
        }

        poseStack.popPose()
    }

    fun vertex(
        pose: PoseStack.Pose,
        consumer: VertexConsumer,
        x: Int,
        y: Int,
        z: Int,
        u: Float,
        v: Float,
        normalX: Int,
        normalY: Int,
        normalZ: Int,
        packedLight: Int
    ) {
        consumer.addVertex(pose, x.toFloat(), y.toFloat(), z.toFloat())
            .setColor(-1)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(pose, normalX.toFloat(), normalZ.toFloat(), normalY.toFloat())
    }
}