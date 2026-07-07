package com.github.nahnullscience.cypher_nexus.client.cypher.renderer

import com.github.nahnullscience.cypher_nexus.client.cypher.state.ItemProjectileRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DedicatedCypherProjectile
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.item.ItemModelResolver
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.entity.projectile.ItemSupplier
import net.minecraft.world.item.ItemDisplayContext

class SimpleItemProjectileRenderer <CE> (
    context: EntityRendererProvider.Context,
) : AbstractCypherRenderer<CE, ItemProjectileRenderState>(context) where CE : DedicatedCypherProjectile, CE : ItemSupplier {
    private val itemModelResolver: ItemModelResolver = context.itemModelResolver

    val defaultScale = 0.5f
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
        poseStack.scale(defaultScale, defaultScale, defaultScale)
        poseStack.scale(state.effectRadius, state.effectRadius, state.effectRadius)
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

    override fun extractRenderState(entity: CE, state: ItemProjectileRenderState, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
        itemModelResolver.updateForNonLiving(state.item, entity.item, ItemDisplayContext.GROUND, entity)
    }
}