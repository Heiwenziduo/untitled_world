package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import com.github.nahnullscience.cypher_nexus.utility.linear_space.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.minus
import com.github.nahnullscience.cypher_nexus.utility.plus
import com.github.nahnullscience.cypher_nexus.utility.times
import com.github.nahnullscience.cypher_nexus.utility.toVec3
import net.minecraft.resources.Identifier
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.math.sqrt

class FrontHexagonPattern(path: Identifier) : AbstractInvokingPattern(path) {
    companion object {
        private const val MAIN = 0.75
        private const val HALF = MAIN / 2
        private val HEIGHT = MAIN * sqrt(3.0) / 2
        private const val Q_RAD = (PI / 3).toFloat()
        private const val A_RAD = (PI / 30).toFloat()

        private val SCRATCH_QUAD = ThreadLocal.withInitial { Quaternionf() }
        private val SCRATCH_AXIS = ThreadLocal.withInitial { Vector3f() }
    }
    override fun layout(
        index: Int,
        total: Int,
        coordinate: AnchoredCoordinate
    ): PosDirePair {
        val i = index % 6
        return coordinate.getOrComputePatternCache(i) cache@{
            val (pos, dir) = posDire

            // 1. Position Calculation (Hexagon Layout)
            val pos1 = when (i) {
                0 -> pos + coordinate.left * MAIN
                1 -> pos + coordinate.left * HALF + coordinate.up * HEIGHT
                2 -> pos + coordinate.right() * HALF + coordinate.up * HEIGHT
                3 -> pos + coordinate.right() * MAIN
                4 -> pos + coordinate.right() * HALF - coordinate.up * HEIGHT
                5 -> pos + coordinate.left * HALF - coordinate.up * HEIGHT
                else -> pos
            }

            // 2. Optimized Direction Rotation (Zero Allocation)
            val q = SCRATCH_QUAD.get()
            val axis = SCRATCH_AXIS.get()

            val frontVec = coordinate.front.toVector3f()

            when (i) {
                0 -> {
                    axis.set(coordinate.up)
                    q.rotationAxis(A_RAD, axis)
                }
                1 -> {
                    axis.set(coordinate.up).rotateAxis(Q_RAD, frontVec.x, frontVec.y, frontVec.z)
                    q.rotationAxis(A_RAD, axis)
                }
                2 -> {
                    axis.set(coordinate.up).rotateAxis(-Q_RAD, frontVec.x, frontVec.y, frontVec.z)
                    q.rotationAxis(-A_RAD, axis)
                }
                3 -> {
                    axis.set(coordinate.up)
                    q.rotationAxis(-A_RAD, axis)
                }
                4 -> {
                    axis.set(coordinate.down()).rotateAxis(Q_RAD, frontVec.x, frontVec.y, frontVec.z)
                    q.rotationAxis(A_RAD, axis)
                }
                5 -> {
                    axis.set(coordinate.down()).rotateAxis(-Q_RAD, frontVec.x, frontVec.y, frontVec.z)
                    q.rotationAxis(-A_RAD, axis)
                }
            }

            val dir1 = dir.toVector3f().rotate(q).toVec3()

            PosDirePair(pos1, dir1)
        }
    }
}