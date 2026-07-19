package com.github.nahnullscience.cypher_nexus.client.cypher.state

import com.github.nahnullscience.cypher_nexus.client.cypher.state.component.CypherRenderState
import com.github.nahnullscience.cypher_nexus.client.cypher.state.component.ICypherEntityRenderState
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.item.ItemStackRenderState

class ItemProjectileRenderState : EntityRenderState(),
    ICypherEntityRenderState by CypherRenderState()
{
    val item: ItemStackRenderState = ItemStackRenderState()
}