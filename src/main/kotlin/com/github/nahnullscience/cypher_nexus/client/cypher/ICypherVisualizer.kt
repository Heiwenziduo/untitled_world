package com.github.nahnullscience.cypher_nexus.client.cypher

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.block.BlockRenderDispatcher
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
interface ICypherVisualizer {
    fun cypher(): AbstractCypher
    fun render(
        projectile: AbstractCypherProjectile,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        itemRenderer: ItemRenderer,
        blockRenderer: BlockRenderDispatcher,
        entityRenderDispatcher: EntityRenderDispatcher,
    )

    interface IBasicItemVisualizer: ICypherVisualizer {
        val stack: ItemStack
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
            poseStack.scale(.5f, .5f, .5f)
            poseStack.mulPose(entityRenderDispatcher.cameraOrientation())
            itemRenderer
                .renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    bufferSource,
                    projectile.level(),
                    projectile.id
                )
            poseStack.popPose()
        }
    }
}