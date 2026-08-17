package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.core.Direction
import net.minecraft.core.Direction.Axis
import net.minecraft.core.Vec3i
import net.minecraft.util.RandomSource
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3d
import org.joml.Vector3f
import kotlin.jvm.optionals.getOrNull
import kotlin.math.*

fun Vec3.toVec3i() = Vec3i(x.toInt(), y.toInt(), z.toInt())
fun Vec3.toV3d() = Vector3d(x, y, z)

operator fun Vec3.unaryMinus() = Vec3(-x, -y, -z)
operator fun Vec3.times(v: Double) = multiply(v, v, v)
operator fun Vec3.times(v: Float) = v.toDouble().let { multiply(it, it, it) }
//operator fun Vec3.plus(v: Vec3?) = if (v == null) this else Vec3(x + v.x, y + v.y, z + v.z)
operator fun Vec3.plus(v: Vec3) = Vec3(x + v.x, y + v.y, z + v.z)
operator fun Vec3.plus(v: Vector3f) = Vec3(x + v.x, y + v.y, z + v.z)
operator fun Vec3.minus(v: Vec3) = Vec3(x - v.x, y - v.y, z - v.z)
operator fun Vec3.minus(v: Vector3f) = Vec3(x - v.x, y - v.y, z - v.z)

operator fun Vec3.component1() = x
operator fun Vec3.component2() = y
operator fun Vec3.component3() = z

fun Vector3f.toVec3(): Vec3 = Vec3(this)
operator fun Vector3f.unaryMinus() = Vector3f(-x, -y, -z)
operator fun Vector3f.times(v: Double) = times(v.toFloat())
operator fun Vector3f.times(v: Float) = Vector3f(x * v, y * v, z * v)
operator fun Vector3f.plus(v: Vector3f) = Vector3f(x + v.x, y + v.y, z + v.z)
fun Vector3f.set(v3: Vec3): Vector3f = set(v3.x, v3.y, v3.z)


//fun Vector3f.rotateAxis(rad: Float, axis: Vec3): Vector3f =
//    rotateAxis(rad, axis.x.toFloat(), axis.y.toFloat(), axis.z.toFloat())

fun Vector3d.toVec3(): Vec3 = Vec3(x, y, z)

fun Vec3.coerceMaxLength(length: Double): Vec3 {
    val lengthSqr = x * x + y * y + z * z
    val maxLenSqr = length * length
    if (lengthSqr <= maxLenSqr) return this

    val ratio = sqrt(maxLenSqr / lengthSqr)
    return Vec3(x * ratio, y * ratio, z * ratio)
}

/**
 * @return the surface of AABB the vector lies, null if they don't overlap
 * */
fun Vec3.getSurfaceOf(aabb: AABB): Direction? {
    val epsilon = 1e-5
    return when {
        abs(x - aabb.minX) < epsilon -> Direction.WEST
        abs(x - aabb.maxX) < epsilon -> Direction.EAST
        abs(y - aabb.minY) < epsilon -> Direction.DOWN
        abs(y - aabb.maxY) < epsilon -> Direction.UP
        abs(z - aabb.minZ) < epsilon -> Direction.NORTH
        abs(z - aabb.maxZ) < epsilon -> Direction.SOUTH
        else -> null
    }
}

/**
 * get Direction through plain value comparison
 * @return [Direction] that the max value of 3 in the vector representing, null if at least 2 values are equal
 * @see [Direction.getApproximateNearest]
 * */
fun Vec3.mostAlignedDirectionExact(): Direction? {
    val axis: Axis
    val positive: Boolean
    val ax = abs(x)
    val ay = abs(y)
    val az = abs(z)
    axis = if (ax > ay && ax > az) {
        positive = ax == x
        Axis.X
    }
    else if (ay > ax && ay > az) {
        positive = ay == y
        Axis.Y
    }
    else if (az > ax && az > ay) {
        positive = az == z
        Axis.Z
    }
    else return null
    return if (positive) axis.positive else axis.negative
}

/**
 * more tolerant version of [mostAlignedDirectionExact], which will flow X -> Y -> Z order when values are equal
 * */
fun Vec3.mostAlignedDirection(): Direction {
    val axis: Axis
    val positive: Boolean
    val ax = abs(x)
    val ay = abs(y)
    val az = abs(z)
    axis = if (ax >= ay && ax >= az) {
        positive = ax == x
        Axis.X
    }
    else if (ay >= ax && ay >= az) {
        positive = ay == y
        Axis.Y
    }
    else {
        positive = az == z
        Axis.Z
    }
    return if (positive) axis.positive else axis.negative
}
/**
 *
 * */
fun Vec3.flipByAxis(axis: Axis, factor: Double = 1.0): Vec3 {
    if (factor == 1.0) return when(axis) {
        Axis.X -> Vec3(-x, y, z)
        Axis.Y -> Vec3(x, -y, z)
        Axis.Z -> Vec3(x, y, -z)
    }
    return when(axis) {
        Axis.X -> Vec3(-x * factor, y, z)
        Axis.Y -> Vec3(x, -y * factor, z)
        Axis.Z -> Vec3(x, y, -z * factor)
    }
}

/**
 * @return new V3 xyz value same as "from", symbol same as "to"
 * */
fun Vec3.toSameDire(to: Vec3): Vec3 {
    return Vec3(
        x.toSameSymbol(to.x),
        y.toSameSymbol(to.y),
        z.toSameSymbol(to.z)
    )
}

fun Vec3.centeredAABB(halfLength: Double): AABB =
    AABB(x - halfLength, y - halfLength, z - halfLength, x + halfLength, y + halfLength, z + halfLength)
fun Vec3.centeredAABB(halfLength: Float): AABB =
    AABB(x - halfLength, y - halfLength, z - halfLength, x + halfLength, y + halfLength, z + halfLength)

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
    return vf.toVec3()
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

/**
 * @return the hit point the given line from this to [destination] collide with [bb], null if not collide
 * */
fun Vec3.rayCastVanilla(destination: Vec3, bb: AABB, margin: Double = Double.NaN): Vec3? {
    val b = if (margin.isNaN()) bb else bb.inflate(margin)
    return b.clip(this, destination).getOrNull()
}

typealias onClip = (clippingPoint: Vec3, direction: Direction?) -> Unit

/**
 * execute [task] if ray pierce the given AABB.
 * direction might be null if both the start and the end vector are inside the AABB.
 * */
inline fun Vec3.rayCastThen(destination: Vec3, bb: AABB, margin: Double = Double.NaN, task: onClip): Boolean {
    val b = if (margin.isNaN()) bb else bb.inflate(margin)
    return b.checkIntersectionThen(this, destination, task)
}

/**
 * direct copy from [AABB.clip], but pass a lambda to utilize the direction
 * */
@Deprecated("this misses the situation that both from and to are inside the box, use [checkAABBIntersection] instead")
inline fun AABB.vanillaAABBClip(from: Vec3, to: Vec3, task: onClip) = vanillaAABBClip(minX, minY, minZ, maxX, maxY, maxZ, from, to, task)
@Deprecated("this misses the situation that both from and to are inside the box, use [checkAABBIntersection] instead")
inline fun vanillaAABBClip(
    minX: Double,
    minY: Double,
    minZ: Double,
    maxX: Double,
    maxY: Double,
    maxZ: Double,
    from: Vec3,
    to: Vec3,
    task: onClip
) {
    val scaleReference = doubleArrayOf(1.0)
    val dx: Double = to.x - from.x
    val dy: Double = to.y - from.y
    val dz: Double = to.z - from.z
    val direction = AABB.getDirection(minX, minY, minZ, maxX, maxY, maxZ, from, scaleReference, null, dx, dy, dz)
    if (direction != null) {
        val scale = scaleReference[0]
        task(from.add(scale * dx, scale * dy, scale * dz), direction)
    }
}

fun AABB.expandToAtMost(to: Vec3, most: Double) =
    expandTowards(
        to.x.coerceIn(-most, +most),
        to.y.coerceIn(-most, +most),
        to.z.coerceIn(-most, +most)
    )


/**
 * Checks line segment intersection with an AABB, passing hit point, direction,
 * and entry face normal to the callback.
 */
inline fun AABB.checkIntersectionThen(from: Vec3, to: Vec3, onIntersect: onClip): Boolean =
    checkAABBIntersection(from, to, minX, minY, minZ, maxX, maxY, maxZ, onIntersect)
inline fun checkAABBIntersection(
    from: Vec3,
    to: Vec3,
    minX: Double,
    minY: Double,
    minZ: Double,
    maxX: Double,
    maxY: Double,
    maxZ: Double,
    onIntersect: onClip
): Boolean {
    val dir = to - from
    val len = dir.lengthSqr()
    if (len < 1e-9) return false

    var tEntry = 0.0
    var tExit = 1.0

    // Tracks the raw unclamped entry time and winning axis for normal calculation
    var lastTMin = -Double.MAX_VALUE
    var normalAxis = 0
    var normalSign = 0.0

    val f = doubleArrayOf(from.x, from.y, from.z)
    val d = doubleArrayOf(dir.x, dir.y, dir.z)
    val bMin = doubleArrayOf(minX, minY, minZ)
    val bMax = doubleArrayOf(maxX, maxY, maxZ)

    for (i in 0..2) {
        if (abs(d[i]) < 1e-9) {
            if (f[i] < bMin[i] || f[i] > bMax[i]) return false
        } else {
            val t1 = (bMin[i] - f[i]) / d[i]
            val t2 = (bMax[i] - f[i]) / d[i]

            val tMinI = min(t1, t2)
            val tMaxI = max(t1, t2)

            // Record which axis was hit last (determines the entry face)
            if (tMinI > lastTMin) {
                lastTMin = tMinI
                normalAxis = i
                normalSign = sign(d[i])
            }

            tEntry = max(tEntry, tMinI)
            tExit = min(tExit, tMaxI)

            if (tEntry > tExit) return false
        }
    }

    if (tEntry <= tExit) {
        val clippingPoint = from + (dir * tEntry)

        // Construct axis-aligned unit normal vector
        val direction =
            if (lastTMin <= 0.0) null // Ray origin is inside the box
            else when (normalAxis) {
                    0 -> directionFromAxisAndSign(Axis.X, normalSign)
                    1 -> directionFromAxisAndSign(Axis.Y, normalSign)
                    else -> directionFromAxisAndSign(Axis.Z, normalSign)
                }


        onIntersect(clippingPoint, direction)
        return true
    }

    return false
}

fun directionFromAxisAndSign(axis: Axis, sign: Double): Direction {
    require(abs(sign) > 1e-12)
    return if (sign > 0) axis.positive else axis.negative
}

fun Axis.randomPerpendicularNormal(random: RandomSource): Vec3 {
    val v = random.nextDouble() * Math.TAU
    return when(this) {
        Axis.X -> Vec3(0.0, sin(v), cos(v))
        Axis.Y -> Vec3(sin(v), 0.0, cos(v))
        Axis.Z -> Vec3(sin(v), cos(v), 0.0)
    }
}
