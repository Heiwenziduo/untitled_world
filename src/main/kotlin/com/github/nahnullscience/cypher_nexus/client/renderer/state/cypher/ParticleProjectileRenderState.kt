package com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher

import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component.CypherRenderState
import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component.ICypherEntityRenderState
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.state.level.QuadParticleRenderState

class ParticleProjectileRenderState : EntityRenderState(), ICypherEntityRenderState by CypherRenderState() {
}