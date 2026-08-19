package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import com.github.nahnullscience.cypher_nexus.utility.linear_space.putCache
import net.minecraft.resources.Identifier
import org.joml.Vector3d
import kotlin.math.PI

class FrontEvenFanPattern(path: Identifier) : AbstractInvokingPattern(path) {
    companion object {
        private const val RAD_HALF = PI / 12
    }

    override fun arrangeVectors(
        index: Int,
        total: Int,
        coordinate: AnchoredCoordinate
    ): Int {
//        val front = coordinate.tmpV3d.set(coordinate.front)
//        val up = coordinate.up
//
//        val stepRad = RAD_HALF / total
//        val rot = -RAD_HALF + (2 * index + 1) * stepRad
//        front.rotateAxis(rot, up.x(), up.y(), up.z())
//
//        coordinate.putCache(0, coordinate.anchor, front)
//        return 0 // well, at least the lagging is authentic

        return (index and 31).also { i ->
            if (coordinate.hasCache(i)) return@also

            val front = coordinate.tmpV3d.set(coordinate.front)
            val up = coordinate.up

            val stepRad = RAD_HALF / total.coerceAtMost(32)
            val rot = -RAD_HALF + (2 * i + 1) * stepRad
            front.rotateAxis(rot, up.x(), up.y(), up.z())

            coordinate.putCache(i, coordinate.anchor, front)
        }
    }
}