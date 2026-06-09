package com.github.nahnullscience.cypher_nexus.client.renderer

import com.github.nahnullscience.cypher_nexus.client.cypher.CypherRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.item.Item

class SimpleItemProjectileRenderer <CY : AbstractCypherProjectile> (
    context: EntityRendererProvider.Context,
    item: Item
) : AbstractCypherRenderer<CY>(context) {


    val scale = 0.5f
    override fun submit(
        state: CypherRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState,
    ) {
        poseStack.pushPose()
        poseStack.scale(scale, scale, scale)
        poseStack.mulPose(camera.orientation)
//        state.item.submit(
//            poseStack,
//            submitNodeCollector,
//            state.lightCoords,
//            OverlayTexture.NO_OVERLAY,
//            state.outlineColor
//        )
        poseStack.popPose()
        super.submit(state, poseStack, submitNodeCollector, camera)
    }
}