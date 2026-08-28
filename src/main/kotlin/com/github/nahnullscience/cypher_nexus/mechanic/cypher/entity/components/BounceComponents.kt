package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components

import com.github.nahnullscience.cypher_nexus.utility.forEachGap
import net.minecraft.world.phys.Vec3
import kotlin.math.sqrt

/**
 * store bounce points.
 *
 * clear at the start of each tick.
 *
 * basically for client side movement interpolation.
 * */
class BouncePointsManager(val capacity: Int) {
    var size: Int = 0
        private set

    var xs: DoubleArray = DoubleArray(capacity)
        private set
    var ys: DoubleArray = DoubleArray(capacity)
        private set
    var zs: DoubleArray = DoubleArray(capacity)
        private set

    fun isEmpty(): Boolean = size == 0
    fun isNotEmpty(): Boolean = size > 0

    fun x(i: Int): Double = xs[i]
    fun y(i: Int): Double = ys[i]
    fun z(i: Int): Double = zs[i]

    fun add(x: Double, y: Double, z: Double): Int {
        xs[size] = x
        ys[size] = y
        zs[size] = z
        return size++
    }

    fun add(bounce: Vec3) = add(bounce.x(), bounce.y(), bounce.z())

    fun clear() {
        size = 0
    }

    fun totalLength(
        startX: Double, startY: Double, startZ: Double,
        endX: Double, endY: Double, endZ: Double,
    ): Double {
        var totalDist = 0.0
        var segStartX = startX; var segStartY = startY; var segStartZ = startZ
        forEachPoint(
            endX, endY, endZ
        ) { _, segEndX, segEndY, segEndZ ->
            val dx = segEndX - segStartX
            val dy = segEndY - segStartY
            val dz = segEndZ - segStartZ
            totalDist += sqrt(dx * dx + dy * dy + dz * dz)
            segStartX = segEndX; segStartY = segEndY; segStartZ = segEndZ
        }
        return totalDist
    }

    inline fun forEachPoint(consumer: (index: Int, x: Double, y: Double, z: Double) -> Unit) {
        for (i in 0 until size) {
            consumer(i, xs[i], ys[i], zs[i])
        }
    }

    inline fun forEachPoint(
        endX: Double, endY: Double, endZ: Double,
        consumer: (index: Int, x: Double, y: Double, z: Double) -> Unit
    ) {
        for (i in 0 until size) {
            consumer(i, xs[i], ys[i], zs[i])
        }
        consumer(size, endX, endY, endZ)
    }

    companion object {

        /**
         * compute the correct interpolated position when trajectory is polyline,
         * where [partial] marks the progress of travel, in range `[0, 1]`.
         * */
        inline fun polylineInterpolate(
            startX: Double, startY: Double, startZ: Double,
            endX: Double, endY: Double, endZ: Double,
            inflections: BouncePointsManager,
            partial: Float,
            crossinline task: (x: Double, y: Double, z: Double) -> Unit
        ) {
            if (partial == 0f) return task(startX, startY, startZ)
            if (partial == 1f) return task(endX, endY, endZ)

            if (inflections.isEmpty()) {
                val dx = (endX - startX) * partial
                val dy = (endY - startY) * partial
                val dz = (endZ - startZ) * partial
                return task(startX + dx, startY + dy, startZ + dz)
            }

            val totalDist = inflections.totalLength(startX, startY, startZ, endX, endY, endZ)
            val expected = totalDist * partial
            var traveled = 0.0
            run travel@ {
                var segStartX = startX; var segStartY = startY; var segStartZ = startZ
                inflections.forEachPoint(
                    endX, endY, endZ
                ) { _, segEndX, segEndY, segEndZ ->
                    val dx = segEndX - segStartX
                    val dy = segEndY - segStartY
                    val dz = segEndZ - segStartZ
                    val segLen = sqrt(dx * dx + dy * dy + dz * dz)
                    if (segLen > 1e-9) {
                        val segEndDist = traveled + segLen
                        if (expected < segEndDist) {
                            val partialPartial = (expected - traveled) / segLen
                            return task(
                                segStartX + dx * partialPartial,
                                segStartY + dy * partialPartial,
                                segStartZ + dz * partialPartial
                            )
                        }
                        traveled = segEndDist
                    }
                    segStartX = segEndX; segStartY = segEndY; segStartZ = segEndZ
                }
            }
        }

        /**
         * @see com.github.nahnullscience.cypher_nexus.utility.forEachGap
         * */
        inline fun forEachGap(
            startX: Double, startY: Double, startZ: Double,
            endX: Double, endY: Double, endZ: Double,
            gap: Double,
            inflections: BouncePointsManager?,
            atLeastOnce: Boolean = true,
            crossinline task: (step: Int, x: Double, y: Double, z: Double) -> Unit,
        ) {
            if (inflections == null || inflections.isEmpty())
                return forEachGap(startX, startY, startZ, endX, endY, endZ, gap, atLeastOnce, task)

            val totalDist = inflections.totalLength(startX, startY, startZ, endX, endY, endZ)
            if (totalDist < gap) {
                if (atLeastOnce) task(0, startX, startY, startZ)
                return
            }

            // walk through poly-lines
            var traveled = 0.0
            var nextSampleDist = 0.0
            var step = 0
            run step@ {
                var segStartX = startX; var segStartY = startY; var segStartZ = startZ
                inflections.forEachPoint(
                    endX, endY, endZ
                ) { _, segEndX, segEndY, segEndZ ->
                    val dx = segEndX - segStartX
                    val dy = segEndY - segStartY
                    val dz = segEndZ - segStartZ
                    val segLen = sqrt(dx * dx + dy * dy + dz * dz)

                    if (segLen > 1e-9) {
                        val segEndDist = traveled + segLen
                        if (nextSampleDist <= segEndDist) {
                            val stepFactor = gap / segLen
                            val stepX = dx * stepFactor
                            val stepY = dy * stepFactor
                            val stepZ = dz * stepFactor

                            val t0 = (nextSampleDist - traveled) / segLen
                            var curX = segStartX + dx * t0
                            var curY = segStartY + dy * t0
                            var curZ = segStartZ + dz * t0

                            while (nextSampleDist <= segEndDist) {
                                task(step, curX, curY, curZ)
                                step++
                                curX += stepX
                                curY += stepY
                                curZ += stepZ
                                nextSampleDist += gap
                            }
                        }
                        traveled = segEndDist
                    }
                    segStartX = segEndX; segStartY = segEndY; segStartZ = segEndZ
                }
            }
        }
    }
}
