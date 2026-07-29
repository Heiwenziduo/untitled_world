package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.world.phys.Vec3

/**
 * leave [direction] normalization to the consumer (e.g. a cypher entity)
 * the direction provider don't have to call [Vec3.normalize]
 * */
data class PosDirePair(
    val position: Vec3,
    val direction: Vec3 = Vec3.ZERO
) {
    fun copy() = PosDirePair(position, direction)
}