package com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher

import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component.CypherRenderState
import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component.ICypherEntityRenderState
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.item.ItemStackRenderState

class ItemProjectileRenderState : EntityRenderState(),
    ICypherEntityRenderState by CypherRenderState()
{
    val item: ItemStackRenderState = ItemStackRenderState()
}