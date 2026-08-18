package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import com.github.nahnullscience.cypher_nexus.utility.linear_space.putCache
import net.minecraft.resources.Identifier
import kotlin.math.PI

class PlaneBifurcatedPattern(path: Identifier) : AbstractInvokingPattern(path) {
    companion object {
        private const val RAD = PI / 9
    }

    override fun arrangeVectors(
        index: Int,
        total: Int,
        coordinate: AnchoredCoordinate
    ): Int {
        return (index and 1).also { i ->
            if (coordinate.hasCache(i)) return@also
            val dire = coordinate.tmpV3d.set(coordinate.front)
            val up = coordinate.up
            if (i == 0) dire.rotateAxis(RAD, up.x(), up.y(), up.z())
            else dire.rotateAxis(RAD, up.x(), up.y(), up.z())

            coordinate.putCache(i, coordinate.anchor, dire)
        }
    }
}