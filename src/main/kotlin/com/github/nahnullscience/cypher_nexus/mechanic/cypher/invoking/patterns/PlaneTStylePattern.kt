//package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns
//
//import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
//import com.github.nahnullscience.cypher_nexus.utility.CoordinateDefinition
//import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
//import net.minecraft.resources.Identifier
//import net.minecraft.world.phys.Vec3
//import org.joml.Quaternionf
//import kotlin.math.PI
//
//class PlaneTStylePattern(path: Identifier) : AbstractInvokingPattern(path) {
//    companion object {
//        private const val RAD = (PI / 2).toFloat()
//    }
//    override fun layout(
//        index: Int,
//        total: Int,
//        coordinate: CoordinateDefinition,
//        posDire: PosDirePair
//    ): PosDirePair {
//        val i = index % 3
//        return when(i) {
//            1 -> {
//                coordinate.getOrComputePatternCache(1) cache@ {
//                    val r = Quaternionf().rotateAxis(RAD, coordinate.top.toVector3f())
//                    val dire = posDire.direction.toVector3f().rotate(r)
//                    return@cache PosDirePair(posDire.position, Vec3(dire))
//                }
//            }
//            2 -> {
//                coordinate.getOrComputePatternCache(2) cache@ {
//                    val r = Quaternionf().rotateAxis(-RAD, coordinate.top.toVector3f())
//                    val dire = posDire.direction.toVector3f().rotate(r)
//                    return@cache PosDirePair(posDire.position, Vec3(dire))
//                }
//            }
//            else -> posDire
//        }
//    }
//}