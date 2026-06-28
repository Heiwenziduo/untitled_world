package com.github.nahnullscience.cypher_nexus.client.cypher.renderer

import com.github.nahnullscience.cypher_nexus.client.cypher.state.EmptyRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractStaticSummoner
import net.minecraft.client.renderer.entity.EntityRendererProvider

class SimpleSummonerRenderer <CY : AbstractStaticSummoner> (
    context: EntityRendererProvider.Context
) : AbstractCypherRenderer<CY, EmptyRenderState>(context) {

    override fun createRenderState() = EmptyRenderState()

}