package com.github.nahnullscience.cypher_nexus.client.cypher.visualizer.modifier

import com.github.nahnullscience.cypher_nexus.client.cypher.ICypherVisualizer
import com.github.nahnullscience.cypher_nexus.content.cypher.modifier.FieryCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.block.BlockRenderDispatcher
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.client.renderer.entity.ItemRenderer

@Deprecated("vanilla entity#displayFireAnimation")
object FieryVi : ICypherVisualizer {
    override fun cypher() = FieryCypher
    override fun render(
        projectile: AbstractCypherProjectile,
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
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation())
        // TODO fire layer
        poseStack.popPose()
    }
}