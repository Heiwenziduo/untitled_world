package com.github.nahnullscience.cypher_nexus.client.renderer

import com.github.nahnullscience.cypher_nexus.client.cypher.CypherProjectileRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.state.level.CameraRenderState

abstract class AbstractCypherRenderer <CY : AbstractCypherProjectile, STATE: CypherProjectileRenderState> (
    context: EntityRendererProvider.Context
) : EntityRenderer<CY, STATE>(context) {

    override fun submit(
        state: STATE,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState,
    ) {
        super.submit(state, poseStack, submitNodeCollector, camera)
    }

    override fun createRenderState(): STATE = CypherProjectileRenderState() as STATE
}