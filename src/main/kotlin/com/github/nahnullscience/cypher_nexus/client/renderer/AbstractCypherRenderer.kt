package com.github.nahnullscience.cypher_nexus.client.renderer

import com.github.nahnullscience.cypher_nexus.client.cypher.CypherRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.state.level.CameraRenderState

abstract class AbstractCypherRenderer <CY : AbstractCypherProjectile> (
    context: EntityRendererProvider.Context
) : EntityRenderer<CY, CypherRenderState>(context) {

    override fun submit(
        state: CypherRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState,
    ) {
        super.submit(state, poseStack, submitNodeCollector, camera)
    }

    override fun createRenderState() = CypherRenderState()
}