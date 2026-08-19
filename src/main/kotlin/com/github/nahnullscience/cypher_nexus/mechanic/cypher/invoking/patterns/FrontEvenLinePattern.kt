package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import com.github.nahnullscience.cypher_nexus.utility.linear_space.putCache
import net.minecraft.resources.Identifier
import org.joml.Vector3d

open class FrontEvenLinePattern(path: Identifier) : AbstractInvokingPattern(path) {
    protected open val stepWidth = 0.25
    val vd0 = Vector3d()

    // ... 3| 1| 0| 2| 4| ...
    override fun arrangeVectors(
        index: Int,
        total: Int,
        coordinate: AnchoredCoordinate
    ): Int {
        return (index % 31).also { i ->
            if (coordinate.hasCache(i)) return@also

            val side = coordinate.tmpV3d.set(coordinate.left)
            val d: Double
            val stepIndex = if (index and 1 == 1) {
                d = 1.0
                (index + 1) / 2
            } else {
                d = -1.0
                index / 2
            }

            val offset = vd0.set(0.0)
            modifyPosition(index, offset, coordinate)

            val pos = side.mulAdd(stepIndex * d * stepWidth, coordinate.anchor).add(offset)
            coordinate.putCache(i, pos, coordinate.front)
        }
    }

    open fun modifyPosition(index: Int, offset: Vector3d, coordinate: AnchoredCoordinate) = Unit

    // o+ >>>>>>
    class StarFleetPattern(path: Identifier) : FrontEvenLinePattern(path) {
        private val stepOffset = -0.25
        private val initOffset = -8 * stepOffset
        override fun modifyPosition(index: Int, offset: Vector3d, coordinate: AnchoredCoordinate) {
            val stepIndex = if (index and 1 == 1) {
                (index + 1) / 2
            } else {
                index / 2
            }
            offset.set(coordinate.front).mul(initOffset + stepOffset * stepIndex)
        }
    }
}