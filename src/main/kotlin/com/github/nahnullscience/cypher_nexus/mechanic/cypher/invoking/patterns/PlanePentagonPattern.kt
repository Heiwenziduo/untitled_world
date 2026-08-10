package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.utility.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.plus
import com.github.nahnullscience.cypher_nexus.utility.set
import com.github.nahnullscience.cypher_nexus.utility.times
import com.github.nahnullscience.cypher_nexus.utility.toVec3
import net.minecraft.resources.Identifier
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.PI

class PlanePentagonPattern(path: Identifier) : AbstractInvokingPattern(path) {
    companion object {
        private const val RAD = (Math.TAU / 5).toFloat()
        private const val LEN = 0.5
        private val Q = Quaternionf()
        private val A = Vector3f()
    }

    override fun layout(
        index: Int,
        total: Int,
        coordinate: CoordinateDefinition,
        posDire: PosDirePair
    ): PosDirePair {
        val i = index % 5
        return coordinate.getOrComputePatternCache(i) cache@ {
            val (pos, dir) = posDire
            A.set(coordinate.up)
            Q.rotationAxis(RAD * i, A)

            val pos1 = pos + A.set(coordinate.front * LEN).rotate(Q)
            val dir1 = A.set(dir).rotate(Q).toVec3()
            PosDirePair(pos1, dir1)
        }
    }
}