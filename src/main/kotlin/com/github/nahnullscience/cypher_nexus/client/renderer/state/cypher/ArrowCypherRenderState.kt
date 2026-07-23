package com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher

import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component.CypherRenderState
import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component.ICypherEntityRenderState
import net.minecraft.client.renderer.entity.state.ArrowRenderState

class ArrowCypherRenderState : ArrowRenderState(), ICypherEntityRenderState by CypherRenderState()
