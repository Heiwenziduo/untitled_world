//package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns
//
//import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
//import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
//import com.github.nahnullscience.cypher_nexus.utility.linear_space.putCache
//import net.minecraft.resources.Identifier
//import kotlin.math.sqrt
//
//class FrontTrianglePattern(path: Identifier) : AbstractInvokingPattern(path) {
//    companion object {
//        private const val BOT = 0.5
//        private const val TOP = BOT / 2
//        private val SIDE = BOT * sqrt(3.0) / 2
//    }
//    override fun arrangeVectors(
//        index: Int,
//        total: Int,
//        coordinate: AnchoredCoordinate
//    ): Int {
//        return (index % 3).also { i ->
//            if (coordinate.hasCache(i)) return@also
//            val vd = coordinate.tmpV3d
//            when(i) {
//                1 -> vd.set(coordinate.anchor).fma(SIDE, coordinate.left).fma(TOP, coordinate.up)
//                2 -> vd.set(coordinate.anchor).fma(-SIDE, coordinate.left).fma(TOP, coordinate.up)
//                else -> vd.set(coordinate.anchor).fma(-BOT, coordinate.up)
//            }
//            coordinate.putCache(i, vd, coordinate.front)
//        }
//    }
//}