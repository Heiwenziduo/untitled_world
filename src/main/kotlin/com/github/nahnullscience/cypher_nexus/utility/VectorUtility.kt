package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

object VectorUtility {
    fun getDireFromHit(hitPoint: Vec3?, aabb: AABB): Direction? {
        if (hitPoint == null) return null
        val epsilon = 1e-5
        return when {
            abs(hitPoint.x - aabb.minX) < epsilon -> Direction.WEST
            abs(hitPoint.x - aabb.maxX) < epsilon -> Direction.EAST
            abs(hitPoint.y - aabb.minY) < epsilon -> Direction.DOWN
            abs(hitPoint.y - aabb.maxY) < epsilon -> Direction.UP
            abs(hitPoint.z - aabb.minZ) < epsilon -> Direction.NORTH
            abs(hitPoint.z - aabb.maxZ) < epsilon -> Direction.SOUTH
            else -> null
        }
    }
}

fun Vec3.toVec3i() = Vec3i(x.toInt(), y.toInt(), z.toInt())

/**
 * @return new V3 xyz value same as "from", symbol same as "to"
 * */
fun Vec3.toSameDire(to: Vec3): Vec3 {
    return Vec3(
        MathUtility.toSameSymbol(this.x, to.x),
        MathUtility.toSameSymbol(this.y, to.y),
        MathUtility.toSameSymbol(this.z, to.z)
    )
}

/**
 * Rotates this vector towards the target vector's direction by at most [maxAngleRadians].
 * The original length of this vector is preserved.
 */
fun Vec3.rotateTowards(target: Vec3, maxAngleRadians: Double): Vec3 {
    val vLenSqr = this.lengthSqr()
    if (vLenSqr < 1e-12) return this

    val dot = this.dot(target)

    val wX = target.x - (dot / vLenSqr) * this.x
    val wY = target.y - (dot / vLenSqr) * this.y
    val wZ = target.z - (dot / vLenSqr) * this.z

    val wLenSqr = wX * wX + wY * wY + wZ * wZ
    if (wLenSqr < 1e-12) {
        // The vectors are collinear
        return this
    }

    val vLen = sqrt(vLenSqr)
    val wLen = sqrt(wLenSqr)

    val totalAngle = atan2(wLen * vLen, dot)
    val angle = min(maxAngleRadians, totalAngle)

    val c = cos(angle)
    val s = sin(angle)

    val scaleW = (vLen * s) / wLen
    return Vec3(
        this.x * c + wX * scaleW,
        this.y * c + wY * scaleW,
        this.z * c + wZ * scaleW
    )
}

