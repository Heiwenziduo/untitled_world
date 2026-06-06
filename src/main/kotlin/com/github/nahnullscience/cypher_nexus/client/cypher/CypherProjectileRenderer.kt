package com.github.nahnullscience.cypher_nexus.client.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.block.BlockRenderDispatcher
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.resources.ResourceLocation
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
class CypherProjectileRenderer(
    context: EntityRendererProvider.Context
) : EntityRenderer<AbstractCypherProjectile>(context) {
    private val itemRenderer: ItemRenderer = context.itemRenderer
    private val blockRenderer: BlockRenderDispatcher = context.blockRenderDispatcher

    override fun render(
        projectile: AbstractCypherProjectile,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int // 0-255
    ) {
        // super.render(projectile, entityYaw, partialTick, poseStack, bufferSource, packedLight)

        poseStack.pushPose()
        // use which egg depends on the visualizer implementation // ??
//        for (modifier in projectile.invokeList) {
//            CypherVisualizerRegistry.get(modifier)?.render(
//                projectile, entityYaw, partialTick, poseStack, bufferSource, packedLight, itemRenderer, blockRenderer, entityRenderDispatcher
//            )
//        }
//        CypherVisualizerRegistry.get(projectile.cypher)?.render(
//            projectile, entityYaw, partialTick, poseStack, bufferSource, packedLight, itemRenderer, blockRenderer, entityRenderDispatcher
//        )

        poseStack.popPose()
    }


    override fun getTextureLocation(entity: AbstractCypherProjectile): ResourceLocation {
        return CypherNexus.modResource("textures/entity/some_texture.png")
    }
}