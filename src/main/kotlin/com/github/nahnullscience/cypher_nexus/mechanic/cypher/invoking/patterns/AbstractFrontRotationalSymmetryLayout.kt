package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.utility.*
import net.minecraft.resources.Identifier
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.PI

abstract class AbstractFrontRotationalSymmetryLayout(path: Identifier) : AbstractInvokingPattern(path) {

    abstract val count: Int
    abstract val initRad: Float
    abstract val stepRad: Float
    abstract val spreadRad: Float
    abstract val dist: Float

    protected val q = Quaternionf()
    protected val v = Vector3f()

    override fun layout(
        index: Int,
        total: Int,
        coordinate: CoordinateDefinition,
        posDire: PosDirePair
    ): PosDirePair {
        val i = index % count
        return coordinate.getOrComputePatternCache(i) cache@ {
            // TODO should omit the "dir" in posDir entirely, refactor that from ShotState
            val (pos , dir) = posDire
            val (xf, yf, zf) = coordinate.front
            val (xl, yl, zl) = coordinate.left

            q.rotationAxis(initRad + stepRad * i, xf.toFloat(), yf.toFloat(), zf.toFloat())
            v.set(coordinate.up).rotate(q)
            val pos1 = pos + v.mul(dist)

            val dir1 = if (spreadRad == 0f) dir else {
                v.set(coordinate.front).rotateAxis(-spreadRad, xl.toFloat(), yl.toFloat(), zl.toFloat())
                v.rotate(q).toVec3()
            }
            PosDirePair(pos1, dir1)
        }
    }

    class FrontSquarePattern(path: Identifier) : AbstractFrontRotationalSymmetryLayout(path) {
        override val count: Int = 4
        override val initRad: Float = -(PI / 4).toFloat()
        override val stepRad: Float = (PI / 2).toFloat()
        override val spreadRad: Float = (PI / 4).toFloat()
        override val dist: Float = 0.25f
    }

    class FrontOctagonPattern(path: Identifier) : AbstractFrontRotationalSymmetryLayout(path) {
        override val count: Int = 8
        override val initRad: Float = -(3 * PI / 8).toFloat()
        override val stepRad: Float = (PI / 4).toFloat()
        override val spreadRad: Float = (PI / 4).toFloat()
        override val dist: Float = 0.5f
    }
}