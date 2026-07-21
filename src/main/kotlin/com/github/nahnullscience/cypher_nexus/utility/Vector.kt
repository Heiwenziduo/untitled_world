package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.function.Predicate
import kotlin.jvm.optionals.getOrNull
import kotlin.math.*

typealias processHit = (hitPoint: Vec3, dir: Direction) -> Unit

fun Vec3.toVec3i() = Vec3i(x.toInt(), y.toInt(), z.toInt())

operator fun Vec3.unaryMinus() = Vec3(-x, -y, -z)
operator fun Vec3.times(v: Double) = multiply(v, v, v)
operator fun Vec3.times(v: Float) = v.toDouble().let { multiply(it, it, it) }
operator fun Vec3.plus(v: Vec3) = Vec3(x + v.x, y + v.y, z + v.z)

operator fun Vector3f.unaryMinus() = Vector3f(-x, -y, -z)
operator fun Vector3f.times(v: Double) = times(v.toFloat())
operator fun Vector3f.times(v: Float) = Vector3f(x * v, y * v, z * v)
operator fun Vector3f.plus(v: Vector3f) = Vector3f(x + v.x, y + v.y, z + v.z)

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
 *
 * */
fun Vec3.flipByDirection(dir: Direction, factor: Double = 1.0): Vec3 {
    return when(dir) {
        Direction.DOWN, Direction.UP -> multiply(1.0, -factor, 1.0)
        Direction.NORTH, Direction.SOUTH -> multiply(1.0, 1.0, -factor)
        Direction.WEST, Direction.EAST -> multiply(-factor, 1.0, 1.0)
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


/**
 * custom projectile hit check function exactly same as [net.minecraft.world.entity.projectile.ProjectileUtil.getHitResult],
 * but avoid magic number "0.3" (margin)
 * */
fun getProjectileHitResult(
    start: Vec3,
    projectile: Entity,
    filter: Predicate<Entity>,
    deltaMovement: Vec3,
    level: Level,
    margin: Float,
    clipContext: ClipContext.Block = ClipContext.Block.COLLIDER
) : HitResult {
    var end = start.add(deltaMovement)
    var hitresult: HitResult = level.clip(
        ClipContext(start, end, clipContext, ClipContext.Fluid.NONE, projectile)
    )
    if (hitresult.type != HitResult.Type.MISS) {
        end = hitresult.getLocation()
    }

    val hitresult1: HitResult? = ProjectileUtil.getEntityHitResult(
        level,
        projectile,
        start,
        end,
        projectile.boundingBox.expandTowards(deltaMovement).inflate(1.0),
        filter,
        margin
    )
    if (hitresult1 != null) {
        hitresult = hitresult1
    }

    return hitresult
}

/**
 * @return the hit point the given line from this to [destination] collide with [bb], null if not collide
 * */
fun Vec3.rayCast(destination: Vec3, bb: AABB, margin: Double): Vec3? {
    return bb.inflate(margin).clip(this, destination).getOrNull()
}

/**
 * immediately execute [task] if ray hit the given AABB
 * */
inline fun Vec3.rayCastThen(destination: Vec3, bb: AABB, margin: Double, task: processHit) {
    bb.inflate(margin).clipWithDirection(this, destination, task)
}

/**
 * direct copy from [AABB.clip], but pass a lambda to utilize the direction
 * */
inline fun AABB.clipWithDirection(from: Vec3, to: Vec3, task: processHit) = clipWithDirection(minX, minY, minZ, maxX, maxY, maxZ, from, to, task)
inline fun AABB.clipWithDirection(
    minX: Double,
    minY: Double,
    minZ: Double,
    maxX: Double,
    maxY: Double,
    maxZ: Double,
    from: Vec3,
    to: Vec3,
    task: processHit
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

