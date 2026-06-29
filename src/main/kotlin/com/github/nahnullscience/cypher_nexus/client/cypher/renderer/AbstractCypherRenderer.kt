package com.github.nahnullscience.cypher_nexus.client.cypher.renderer

import com.github.nahnullscience.cypher_nexus.client.cypher.state.ICypherEntityRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DedicatedCypherProjectile
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState

abstract class AbstractCypherRenderer <CY : DedicatedCypherProjectile, State> (
    context: EntityRendererProvider.Context
) : EntityRenderer<CY, State>(context) where State : EntityRenderState, State : ICypherEntityRenderState {

    override fun extractRenderState(entity: CY, state: State, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
//        state.radius =
    }
}