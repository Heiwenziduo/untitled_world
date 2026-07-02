package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.util.RandomSource
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.*


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
 *
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

/**
 * @return normalized vector randomly distributed within a shape of cone
 * */
fun Vec3.randomInCone(maxAngle: Double, random: RandomSource): Vec3 {
    val vf = this.toVector3f().randomInCone(maxAngle, random)
    return Vec3(vf.x.toDouble(), vf.y.toDouble(), vf.z.toDouble())
}

/**
 * Generates a random vector within a cone around an initial arbitrary vector.
 * No normalize needed for the initial vector
 * @param maxAngle The maximum spread angle in Degree.
 * @param random [RandomSource]
 * @return A new normalized Vector3f pointing somewhere inside the cone.
 */
fun Vector3f.randomInCone(maxAngle: Double, random: RandomSource): Vector3f {
    val r = Math.toRadians(maxAngle).coerceIn(0.0, PI) // the meaningful domain is [0, pi]


    // 1. Calculate the local uniform random vector in a Z-up cone
    val u1: Float = random.nextFloat()
    val u2: Float = random.nextFloat()


    // Use cosine distribution to avoid clumping at the center
    val z = 1.0f - u2 * (1.0f - cos(r).toFloat())
    val xyMag = sqrt((1.0f - z * z).toDouble()).toFloat()
    val theta = 2.0f * Math.PI.toFloat() * u1

    val x = xyMag * cos(theta.toDouble()).toFloat()
    val y = xyMag * sin(theta.toDouble()).toFloat()

    val localRandomDir = Vector3f(x, y, z)


    // 2. Prepare the target rotation
    val standardZ = Vector3f(0f, 0f, 1f)
    val targetDir = Vector3f(this).normalize()


    // 3. Overcome Gimbal Lock using JOML's Quaternionf.
    // rotationTo() automatically calculates the single arbitrary axis and angle
    // needed to rotate standardZ into targetDir, bypassing Euler angles completely!
    val rotationQuat = Quaternionf().rotationTo(standardZ, targetDir)


    // 4. Apply the Quaternion rotation to our local vector
    localRandomDir.rotate(rotationQuat)

    return localRandomDir
}

