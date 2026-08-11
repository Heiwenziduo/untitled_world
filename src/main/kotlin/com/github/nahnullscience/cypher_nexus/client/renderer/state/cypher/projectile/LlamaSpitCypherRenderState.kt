package com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.projectile

import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component.CypherRenderStateDelegate
import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component.ICypherEntityRenderState
import net.minecraft.client.renderer.entity.state.LlamaSpitRenderState

class LlamaSpitCypherRenderState : LlamaSpitRenderState(), ICypherEntityRenderState by CypherRenderStateDelegate() {
}