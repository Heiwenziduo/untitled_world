package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.utility.*
import net.minecraft.resources.Identifier
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.PI

abstract class AbstractPerpendicularPattern(path: Identifier) : AbstractInvokingPattern(path) {
    companion object {
        private const val P2 = -(PI / 2).toFloat()
    }

    private val q = Quaternionf()
    private val a = Vector3f()

    abstract val round: Int
    abstract val radBase: Float
    abstract val rad: Float
    abstract val len: Float

    final override fun layout(
        index: Int,
        total: Int,
        coordinate: CoordinateDefinition,
        posDire: PosDirePair
    ): PosDirePair {
        val i = index and (round - 1)
        return coordinate.getOrComputePatternCache(i) cache@ {
            val (pos, dir) = posDire
            val (xf, yf, zf) = coordinate.front
            val (xl, yl, zl) = coordinate.left

            a.set(coordinate.up)
             .rotateAxis(radBase + rad * i, xf.toFloat(), yf.toFloat(), zf.toFloat())
             .mul(len)
            q.rotationAxis(radBase + rad * i, xf.toFloat(), yf.toFloat(), zf.toFloat())
             .rotateAxis(P2, xl.toFloat(), yl.toFloat(), zl.toFloat())

            val pos1 = pos + a
            val dir1 = a.set(dir).rotate(q).toVec3()
            PosDirePair(pos1, dir1)
        }
    }

    class Square(path: Identifier) : AbstractPerpendicularPattern(path) {
        override val round: Int = 4
        override val radBase: Float = -(PI / 4).toFloat()
        override val rad: Float = (Math.TAU / 4).toFloat()
        override val len: Float = 0.25f
    }

    class Octagon(path: Identifier) : AbstractPerpendicularPattern(path) {
        override val round: Int = 8
        override val radBase: Float = -(3 * PI / 8).toFloat()
        override val rad: Float = (Math.TAU / 8).toFloat()
        override val len: Float = 0.5f
    }
}