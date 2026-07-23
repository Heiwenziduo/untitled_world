package com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component

import net.minecraft.client.renderer.entity.state.EntityRenderState

/**
 * mark those projectiles that are invisible
 * */
class EmptyRenderState : EntityRenderState(), ICypherEntityRenderState by CypherRenderState() {
}