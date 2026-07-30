package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.world.phys.Vec3

/**
 * assume [front] & [left] are normalized
 *
 * */
data class CoordinateDefinition(
    val front: Vec3,
    val left: Vec3
) {
    val top = front.cross(left)

    init {

    }
}