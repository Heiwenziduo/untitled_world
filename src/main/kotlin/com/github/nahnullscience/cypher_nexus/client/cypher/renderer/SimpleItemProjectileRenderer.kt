package com.github.nahnullscience.cypher_nexus.client.cypher.renderer

import com.github.nahnullscience.cypher_nexus.client.cypher.state.ItemProjectileRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.utility.times
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.item.ItemModelResolver
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.entity.projectile.ItemSupplier
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

class SimpleItemProjectileRenderer <CE> (
    context: EntityRendererProvider.Context,
) : AbstractCypherRenderer<CE, ItemProjectileRenderState>(context) where CE : AbstractDedicatedCypherProjectile, CE : ItemSupplier {

    companion object {
        const val DEFAULT_SCALE = 0.5f
        // these two translate put item in the center of a bounding box, no matter how it scaled
        private val translatePost = Vector3f(0f, -0.125f, 0f)
        private val translatePre = translatePost * -DEFAULT_SCALE
    }

    private val itemModelResolver: ItemModelResolver = context.itemModelResolver

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
        poseStack.translate(translatePre)
        poseStack.cypherSetup(state, submitNodeCollector, camera)
        poseStack.scale(DEFAULT_SCALE, DEFAULT_SCALE, DEFAULT_SCALE)
        poseStack.mulPose(camera.orientation)
        poseStack.translate(translatePost)
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