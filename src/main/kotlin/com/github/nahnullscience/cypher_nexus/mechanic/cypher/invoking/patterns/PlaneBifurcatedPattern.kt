package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.utility.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import net.minecraft.resources.Identifier
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.math.sign

class PlaneBifurcatedPattern(path: Identifier) : AbstractInvokingPattern(path) {
    companion object {
        private const val RAD = (PI / 9).toFloat()
//        private val quaternions = listOf(Quaternionf(), Quaternionf())
//        init {
//            quaternions.forEachIndexed { index, quaternion ->
//                val r = sign(index.toFloat() - 0.5f) * RAD
//                quaternion.rotateAxis(r, Vector3f(0f, 1f, 0f))
//            }
//        }
    }

    override fun layout(
        index: Int,
        total: Int,
        coordinate: CoordinateDefinition,
        posDire: PosDirePair
    ): PosDirePair {
        val i = index and 1
        return when (i) {
            0 -> {
                coordinate.getOrComputePatternCache(0) cache@ {
//                    val r = Quaternionf().rotateAxis(RAD, coordinate.top.toVector3f())
//                    val r = quaternions[0]
//                    val dire = posDire.direction.toVector3f().rotate(r)
                    val dire = coordinate.rightScrewFromTop(posDire.direction, RAD)
                    return@cache PosDirePair(posDire.position, dire)
                }
            }
            else -> {
                coordinate.getOrComputePatternCache(1) cache@ {
//                    val r = Quaternionf().rotateAxis(-RAD, coordinate.top.toVector3f())
//                    val r = quaternions[1]
//                    val dire = posDire.direction.toVector3f().rotate(r)
                    val dire = coordinate.rightScrewFromTop(posDire.direction, -RAD)
                    return@cache PosDirePair(posDire.position, dire)
                }
            }
        }
    }
}