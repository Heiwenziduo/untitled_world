package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import com.github.nahnullscience.cypher_nexus.utility.linear_space.putCache
import net.minecraft.resources.Identifier
import org.joml.Vector3d

abstract class AbstractPlaneRotationalSymmetryPattern(
    path: Identifier,
    val count: Int,
    val initRad: Double,
    val dist: Double
) : AbstractInvokingPattern(path) {
    open val stepRad: Double = Math.TAU / count

    protected val vd2 = Vector3d()
    override fun arrangeVectors(index: Int, total: Int, coordinate: AnchoredCoordinate): Int {
        return (index % count).also { i ->
            if (coordinate.hasCache(i)) return@also
            val qd = coordinate.tmpQd
            val vd = coordinate.tmpV3d

            vd.set(coordinate.up)
            qd.rotationAxis(initRad + stepRad * i, vd.x, vd.y, vd.z)
            vd2.set(coordinate.front).rotate(qd)
            vd.set(vd2).mulAdd(dist, coordinate.anchor)
            coordinate.putCache(i, vd, vd2)
        }
    }

    class PlanePentagonPattern(path: Identifier) : AbstractPlaneRotationalSymmetryPattern(
        path,
        count = 5,
        initRad = 0.0,
        dist = 0.5
    )
}