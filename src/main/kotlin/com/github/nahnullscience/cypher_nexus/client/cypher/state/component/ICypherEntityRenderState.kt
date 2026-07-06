package com.github.nahnullscience.cypher_nexus.client.cypher.state.component

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

/**
 *
 * */
interface ICypherEntityRenderState {
    val flags: Int
    val effectRadius: Float
    val bouncePoints: List<Vec3>
    fun <CE> extractFrom(cy: CE) where CE : Entity, CE : ICypherEntity
}