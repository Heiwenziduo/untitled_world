package com.github.nahnullscience.cypher_nexus.client.cypher.renderer

import com.github.nahnullscience.cypher_nexus.client.cypher.state.CypherProjectileRenderState
import com.github.nahnullscience.cypher_nexus.content.entity.LlamaSpit
import net.minecraft.client.renderer.entity.EntityRendererProvider

class LlamaSpitCypherRenderer (
    context: EntityRendererProvider.Context
) : AbstractCypherRenderer<LlamaSpit, CypherProjectileRenderState>(context) {

}