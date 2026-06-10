package com.github.nahnullscience.cypher_nexus.client.renderer

import com.github.nahnullscience.cypher_nexus.client.cypher.ItemProjectileRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.item.ItemModelResolver
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.entity.projectile.ItemSupplier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

class SimpleItemProjectileRenderer <CY> (
    context: EntityRendererProvider.Context,
) : AbstractCypherRenderer<CY, ItemProjectileRenderState>(context) where CY : AbstractCypherProjectile, CY : ItemSupplier {
    private val itemModelResolver: ItemModelResolver = context.itemModelResolver
//    val stack = ItemStack(item)
    val scale = 0.5f

    init {
        // println("SimpleItemProjectileRenderer init") // called when load into main menu
    }

    override fun submit(
        state: ItemProjectileRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState,
    ) {
        poseStack.pushPose()
        poseStack.scale(scale, scale, scale)
        poseStack.mulPose(camera.orientation)
        state.item.submit(
            poseStack,
            submitNodeCollector,
            state.lightCoords,
            OverlayTexture.NO_OVERLAY,
            state.outlineColor
        )
        poseStack.popPose()
        super.submit(state, poseStack, submitNodeCollector, camera)
    }

    override fun createRenderState() = ItemProjectileRenderState()

    override fun extractRenderState(entity: CY, state: ItemProjectileRenderState, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
        itemModelResolver.updateForNonLiving(state.item, entity.item, ItemDisplayContext.GROUND, entity)
    }
}