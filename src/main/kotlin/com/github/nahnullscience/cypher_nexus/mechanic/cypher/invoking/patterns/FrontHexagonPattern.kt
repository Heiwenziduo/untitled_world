package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.utility.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.minus
import com.github.nahnullscience.cypher_nexus.utility.plus
import com.github.nahnullscience.cypher_nexus.utility.times
import com.github.nahnullscience.cypher_nexus.utility.toVec3
import net.minecraft.resources.Identifier
import org.joml.Quaternionf
import kotlin.math.PI
import kotlin.math.sqrt

class FrontHexagonPattern(path: Identifier) : AbstractInvokingPattern(path) {
    companion object {
        private const val MAIN = 0.5
        private const val HALF = MAIN / 2
        private val HEIGHT = MAIN * sqrt(3.0) / 2
        private const val Q_RAD = (PI / 3).toFloat()
        private const val A_RAD = (PI / 36).toFloat()
    }
    override fun layout(
        index: Int,
        total: Int,
        coordinate: CoordinateDefinition,
        posDire: PosDirePair
    ): PosDirePair {
        val i = index % 6
        return coordinate.getOrComputePatternCache(i) cache@{
            when (i) {
                0 -> {
                    val (pos, dir) = posDire
                    val a = coordinate.up.toVector3f()
                    val q2 = Quaternionf().rotateAxis(A_RAD, a)
                    val pos1 = pos + coordinate.left * MAIN
                    val dir1 = dir.toVector3f().rotate(q2).toVec3()
                    PosDirePair(pos1, dir1)
                }
                1 -> {
                    val (pos, dir) = posDire
                    val front = coordinate.front.toVector3f()
                    val q = Quaternionf().rotateAxis(Q_RAD, front)
                    val a = coordinate.up.toVector3f().rotate(q)
                    val q2 = Quaternionf().rotateAxis(A_RAD, a)
                    val pos1 = pos + coordinate.left * HALF + coordinate.up * HEIGHT
                    val dir1 = dir.toVector3f().rotate(q2).toVec3()
                    PosDirePair(pos1, dir1)
                }
                2-> {
                    val (pos, dir) = posDire
                    val front = coordinate.front.toVector3f()
                    val q = Quaternionf().rotateAxis(-Q_RAD, front)
                    val a = coordinate.up.toVector3f().rotate(q)
                    val q2 = Quaternionf().rotateAxis(-A_RAD, a)
                    val pos1 = pos + coordinate.right() * HALF + coordinate.up * HEIGHT
                    val dir1 = dir.toVector3f().rotate(q2).toVec3()
                    PosDirePair(pos1, dir1)
                }

                3-> {
                    val (pos, dir) = posDire
                    val a = coordinate.up.toVector3f()
                    val q2 = Quaternionf().rotateAxis(-A_RAD, a)
                    val pos1 = pos + coordinate.right() * MAIN
                    val dir1 = dir.toVector3f().rotate(q2).toVec3()
                    PosDirePair(pos1, dir1)
                }
                4-> {
                    val (pos, dir) = posDire
                    val front = coordinate.front.toVector3f()
                    val q = Quaternionf().rotateAxis(Q_RAD, front)
                    val a = coordinate.down().toVector3f().rotate(q)
                    val q2 = Quaternionf().rotateAxis(A_RAD, a)
                    val pos1 = pos + coordinate.right() * HALF - coordinate.up * HEIGHT
                    val dir1 = dir.toVector3f().rotate(q2).toVec3()
                    PosDirePair(pos1, dir1)
                }
                5-> {
                    val (pos, dir) = posDire
                    val front = coordinate.front.toVector3f()
                    val q = Quaternionf().rotateAxis(-Q_RAD, front)
                    val a = coordinate.down().toVector3f().rotate(q)
                    val q2 = Quaternionf().rotateAxis(-A_RAD, a)
                    val pos1 = pos + coordinate.left * HALF - coordinate.up * HEIGHT
                    val dir1 = dir.toVector3f().rotate(q2).toVec3()
                    PosDirePair(pos1, dir1)
                }
                else -> posDire
            }
        }
    }
}