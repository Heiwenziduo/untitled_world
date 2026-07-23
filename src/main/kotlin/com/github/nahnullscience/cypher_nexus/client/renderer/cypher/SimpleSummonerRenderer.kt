package com.github.nahnullscience.cypher_nexus.client.renderer.cypher

import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component.EmptyRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractStaticSummoner
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context

class SimpleSummonerRenderer <CY : AbstractStaticSummoner> (
    context: Context
) : AbstractCypherRenderer<CY, EmptyRenderState>(context) {

    override fun createRenderState() = EmptyRenderState()

}