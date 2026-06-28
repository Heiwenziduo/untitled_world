package com.github.nahnullscience.cypher_nexus.client.cypher.state

import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.item.ItemStackRenderState

class ItemProjectileRenderState : EntityRenderState(), ICypherEntityRenderState {
    val item: ItemStackRenderState = ItemStackRenderState()
}