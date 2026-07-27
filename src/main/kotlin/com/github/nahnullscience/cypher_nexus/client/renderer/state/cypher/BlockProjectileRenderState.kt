package com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher

import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component.CypherRenderStateDelegate
import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component.ICypherEntityRenderState
import net.minecraft.client.renderer.block.BlockModelRenderState
import net.minecraft.client.renderer.entity.state.EntityRenderState

class BlockProjectileRenderState : EntityRenderState(),
    ICypherEntityRenderState by CypherRenderStateDelegate()
{
    val block: BlockModelRenderState = BlockModelRenderState()
}