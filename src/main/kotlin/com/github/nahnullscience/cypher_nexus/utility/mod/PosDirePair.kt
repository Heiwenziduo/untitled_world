package com.github.nahnullscience.cypher_nexus.utility.mod

import net.minecraft.world.phys.Vec3

data class PosDirePair(
    val position: Vec3,
    val direction: Vec3 = Vec3.ZERO
)