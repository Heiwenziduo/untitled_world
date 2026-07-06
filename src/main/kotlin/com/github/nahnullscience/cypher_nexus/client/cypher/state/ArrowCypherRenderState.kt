package com.github.nahnullscience.cypher_nexus.client.cypher.state

import com.github.nahnullscience.cypher_nexus.client.cypher.state.component.CypherRenderState
import com.github.nahnullscience.cypher_nexus.client.cypher.state.component.ICypherEntityRenderState
import net.minecraft.client.renderer.entity.state.ArrowRenderState

class ArrowCypherRenderState : ArrowRenderState(), ICypherEntityRenderState by CypherRenderState()
