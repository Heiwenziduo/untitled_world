package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import com.github.nahnullscience.cypher_nexus.utility.linear_space.putCache
import net.minecraft.resources.Identifier
import kotlin.math.PI

open class PlaneTrifurcatedPattern(path: Identifier) : AbstractInvokingPattern(path) {
    protected open val rad = PI / 6
    override fun arrangeVectors(
        index: Int,
        total: Int,
        coordinate: AnchoredCoordinate
    ): Int {
        return (index % 3).also { i ->
            if (coordinate.hasCache(i)) return@also
            val dire = coordinate.tmpV3d.set(coordinate.front)
            val up = coordinate.up
            when (i) {
                1 -> dire.rotateAxis(rad, up.x(), up.y(), up.z())
                2 -> dire.rotateAxis(-rad, up.x(), up.y(), up.z())
            }
            coordinate.putCache(i, coordinate.anchor, dire)
        }
    }

    class PlaneTStylePattern(path: Identifier) : PlaneTrifurcatedPattern(path) {
        override val rad = PI / 2
    }
}