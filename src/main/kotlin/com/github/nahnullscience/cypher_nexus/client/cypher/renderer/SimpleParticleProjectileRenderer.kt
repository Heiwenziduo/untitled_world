package com.github.nahnullscience.cypher_nexus.client.cypher.renderer

import com.github.nahnullscience.cypher_nexus.client.cypher.state.ParticleProjectileRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import net.minecraft.client.renderer.entity.EntityRendererProvider

class SimpleParticleProjectileRenderer  <CY : AbstractDedicatedCypherProjectile> (
    context: EntityRendererProvider.Context
) : AbstractCypherRenderer<CY, ParticleProjectileRenderState>(context) {

    override fun createRenderState() = ParticleProjectileRenderState()

}