package com.github.nahnullscience.cypher_nexus.client.cypher.state

import net.minecraft.client.renderer.block.BlockModelRenderState
import net.minecraft.client.renderer.entity.state.EntityRenderState

class BlockProjectileRenderState : EntityRenderState(), ICypherEntityRenderState {
    val block: BlockModelRenderState = BlockModelRenderState()
}