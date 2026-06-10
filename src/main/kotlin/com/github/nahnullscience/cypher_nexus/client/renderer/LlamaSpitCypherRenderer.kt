package com.github.nahnullscience.cypher_nexus.client.renderer

import com.github.nahnullscience.cypher_nexus.client.cypher.CypherProjectileRenderState
import com.github.nahnullscience.cypher_nexus.content.entity.LlamaSpit
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider

class LlamaSpitCypherRenderer (
    context: EntityRendererProvider.Context
) : AbstractCypherRenderer<LlamaSpit, CypherProjectileRenderState>(context) {

}