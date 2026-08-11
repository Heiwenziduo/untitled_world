package com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.world.entity.Entity

/**
 * mark those projectiles that are invisible
 * */
class EmptyRenderState : EntityRenderState(), ICypherEntityRenderState by CypherRenderStateDelegate() {
    override fun <CE> extractFrom(ce: CE, state: EntityRenderState) where CE : Entity, CE : ICypherEntity = Unit
}