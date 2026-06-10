package com.github.nahnullscience.cypher_nexus.client.renderer

import com.github.nahnullscience.cypher_nexus.client.cypher.CypherProjectileRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractStaticSummoner
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider

class SimpleSummonerRenderer <CY : AbstractStaticSummoner> (
    context: EntityRendererProvider.Context
) : AbstractCypherRenderer<CY, CypherProjectileRenderState>(context) {

}