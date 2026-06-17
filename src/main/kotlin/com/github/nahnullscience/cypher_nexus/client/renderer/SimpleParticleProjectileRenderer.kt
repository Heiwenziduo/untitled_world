package com.github.nahnullscience.cypher_nexus.client.renderer

import com.github.nahnullscience.cypher_nexus.client.cypher.CypherProjectileRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import net.minecraft.client.renderer.entity.EntityRendererProvider

class SimpleParticleProjectileRenderer  <CY : AbstractCypherProjectile> (
    context: EntityRendererProvider.Context
) : AbstractCypherRenderer<CY, CypherProjectileRenderState>(context) {

}