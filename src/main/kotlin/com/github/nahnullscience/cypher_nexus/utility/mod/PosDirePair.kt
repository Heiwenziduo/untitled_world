package com.github.nahnullscience.cypher_nexus.utility.mod

import net.minecraft.world.phys.Vec3

data class PosDirePair(
    val position: Vec3,
    /**
     * leave normalization to the consumer
     *
     * the direction provider don't have to call [Vec3.normalize]
     * */
    val direction: Vec3 = Vec3.ZERO
)