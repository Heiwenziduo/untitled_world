package com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

/**
 *
 * */
interface ICypherEntityRenderState {
    val flags: Int
    val effectRadius: Float
    val bouncePoints: List<Vec3>
    val deltaMove: Vec3
    fun <CE> extractFrom(ce: CE, state: EntityRenderState) where CE : Entity, CE : ICypherEntity
}