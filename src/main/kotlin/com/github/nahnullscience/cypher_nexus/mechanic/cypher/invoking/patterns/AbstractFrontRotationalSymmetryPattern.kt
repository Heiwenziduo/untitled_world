package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import com.github.nahnullscience.cypher_nexus.utility.linear_space.putCache
import net.minecraft.resources.Identifier
import org.joml.Vector3d
import kotlin.math.PI

abstract class AbstractFrontRotationalSymmetryPattern(
    path: Identifier,
    val count: Int,
    val initRad: Double,
    val diffuseRad: Double,
    val dist: Double
) : AbstractInvokingPattern(path) {
    open val stepRad: Double = Math.TAU / count

    protected val vd2 = Vector3d()
    override fun arrangeVectors(
        index: Int,
        total: Int,
        coordinate: AnchoredCoordinate
    ): Int {
        return (index % count).also { i ->
            if (coordinate.hasCache(i)) return@also
            val qd = coordinate.tmpQd
            val vd = coordinate.tmpV3d
            val front = coordinate.front
            val left = coordinate.left

            qd.rotationAxis(initRad + stepRad * i, front.x(), front.y(), front.z())
            vd.set(coordinate.up).rotate(qd).mulAdd(dist, coordinate.anchor)
            vd2.set(coordinate.front)
            if (diffuseRad != 0.0) vd2.rotateAxis(-diffuseRad, left.x(), left.y(), left.z()).rotate(qd)
            coordinate.putCache(i, vd, vd2)
        }
    }

    class FrontTrianglePattern(path: Identifier) : AbstractFrontRotationalSymmetryPattern(
        path,
        count = 3,
        initRad = -PI / 3,
        diffuseRad = 0.0,
        dist = 0.33
    )

    class FrontHexagonPattern(path: Identifier) : AbstractFrontRotationalSymmetryPattern(
        path,
        count = 6,
        initRad = -PI / 4,
        diffuseRad = PI / 30,
        dist = 0.5
    )

    class FrontDiffuseSquarePattern(path: Identifier) : AbstractFrontRotationalSymmetryPattern(
        path,
        count = 4,
        initRad = -PI / 4,
        diffuseRad = PI / 4,
        dist = 0.25,
    )

    class FrontDiffuseHexagonPattern(path: Identifier) : AbstractFrontRotationalSymmetryPattern(
        path,
        count = 6,
        initRad = -PI / 4,
        diffuseRad = PI / 4,
        dist = 0.5,
    )

    class FrontDiffuseOctagonPattern(path: Identifier) : AbstractFrontRotationalSymmetryPattern(
        path,
        count = 8,
        initRad = -3 * PI / 8,
        diffuseRad = PI / 4,
        dist = 0.75,
    )

    class PerpendicularSquarePattern(path: Identifier) : AbstractFrontRotationalSymmetryPattern(
        path,
        count = 4,
        initRad = PI / 4,
        diffuseRad = PI / 2,
        dist = 0.25,
    )

    class PerpendicularOctagonPattern(path: Identifier) : AbstractFrontRotationalSymmetryPattern(
        path,
        count = 8,
        initRad = -3 * PI / 8,
        diffuseRad = PI / 2,
        dist = 0.75,
    )
}