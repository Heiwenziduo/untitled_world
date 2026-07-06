package com.github.nahnullscience.cypher_nexus.client.cypher.renderer

import com.github.nahnullscience.cypher_nexus.client.cypher.state.component.ICypherEntityRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.world.entity.Entity

abstract class AbstractCypherRenderer <CE, State> (
    context: EntityRendererProvider.Context
) : EntityRenderer<CE, State>(context)
        where CE : Entity, CE : ICypherEntity,
              State : EntityRenderState, State : ICypherEntityRenderState
{
    override fun extractRenderState(entity: CE, state: State, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
        state.extractFrom(entity)
    }
}