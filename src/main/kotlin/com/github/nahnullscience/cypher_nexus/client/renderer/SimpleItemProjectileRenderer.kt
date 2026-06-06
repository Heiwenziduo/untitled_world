package com.github.nahnullscience.cypher_nexus.client.renderer

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

class SimpleItemProjectileRenderer <CY : AbstractCypherProjectile> (
    context: EntityRendererProvider.Context,
    item: Item
) : AbstractCypherRenderer<CY>(context) {
    private val itemRenderer: ItemRenderer = context.itemRenderer
    private val stack = ItemStack(item)

    override fun render(
        projectile: CY,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
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