package com.github.nahnullscience.cypher_nexus.utility

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import com.github.nahnullscience.cypher_nexus.utility.linear_space.anchor
import com.github.nahnullscience.cypher_nexus.utility.linear_space.fromFrontLeftOrthonormal
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.TraceableEntity
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object EntityUtil {
    /** check if A & B is adversarial  */
    fun isEnemy(A: LivingEntity, B: LivingEntity) : Boolean {
        if (A.`is`(B)) return false
        if (!A.isAttackable || !B.isAttackable) return false
//        if (A.isInvulnerable)
        return true
    }
}

inline val Entity.level get() = level()

fun Entity.getDimensions() = getDimensions(this.pose)

fun <CE> Entity.isOwnerOf(ce: CE): Boolean where CE : Entity, CE : ICypherEntity {
    if (this == ce.owner) return true
    ce.owner?.let {
        if (it is TraceableEntity && it.owner == this) return true
    }
    return false
}

/**
 * a direct duplication of [Projectile.updateRotation]
 * */
fun Entity.rotateTowardSpeed(factor: Float) {
    val movement = this.deltaMovement
    val sd = movement.horizontalDistance()
    this.xRot = lerpRotation(
        this.xRotO,
        (Mth.atan2(movement.y, sd) * 180.0f / Math.PI.toFloat()).toFloat(),
        factor
    )
    this.yRot = lerpRotation(
        this.yRotO,
        (Mth.atan2(movement.x, movement.z) * 180.0f / Math.PI.toFloat()).toFloat(),
        factor
    )
}

private fun lerpRotation(from: Float, to: Float, factor: Float): Float {
    var rotO = from
    while (to - rotO < -180.0f) {
        rotO -= 360.0f
    }

    while (to - rotO >= 180.0f) {
        rotO += 360.0f
    }

    return Mth.lerp(factor, rotO, to)
}

/**
 * @return `normalized` vector pointing upward from the top of the entity cranium.
 * utilize `Quaternions` to avoid gimbal-lock
 * */
fun Entity.headUpVector(partialTick: Float = 1.0f): Vec3 {
    val pitchRad = Math.toRadians(getViewXRot(partialTick).toDouble()).toFloat()
    val yawRad = Math.toRadians(getViewYRot(partialTick).toDouble()).toFloat()

    val rotation = Quaternionf().rotationYXZ(-yawRad, pitchRad, 0.0f)
    val localUp = Vector3f(0.0f, 1.0f, 0.0f).rotate(rotation)
    return Vec3(localUp.x().toDouble(), localUp.y().toDouble(), localUp.z().toDouble())
}

/**
 * v3f version of [headUpVector]
 * */
fun Entity.headUpVectorF(partialTick: Float = 1.0f): Vector3f {
    val pitchRad = Math.toRadians(getViewXRot(partialTick).toDouble()).toFloat()
    val yawRad = Math.toRadians(getViewYRot(partialTick).toDouble()).toFloat()

    val rotation = Quaternionf().rotationYXZ(-yawRad, pitchRad, 0.0f)
    val localUp = Vector3f(0.0f, 1.0f, 0.0f).rotate(rotation)
    return localUp
}

/**
 * @return `normalized` vector pointing leftward from the perspective of the entity
 * @see headUpVector
 * */
fun Entity.headLeftVector(partialTick: Float = 1.0f): Vec3 {
    val yawRad = getViewYRot(partialTick) * Mth.DEG_TO_RAD

    return Vec3(
        Mth.cos(yawRad.toDouble()).toDouble(),
        0.0,
        Mth.sin(yawRad.toDouble()).toDouble()
    )
}

/**
 * v3f version of [headLeftVector]
 * */
fun Entity.headLeftVectorF(partialTick: Float = 1.0f): Vector3f {
    val yawRad = getViewYRot(partialTick) * Mth.DEG_TO_RAD

    return Vector3f(
        Mth.cos(yawRad.toDouble()),
        0f,
        Mth.sin(yawRad.toDouble())
    )
}

/**
 * create a coordinate by someone's perspective
 *
 * front : entity's head view vector. [Entity.getHeadLookAngle]
 *
 * left : entity's head left vector. [headLeftVector]
 * */
fun Entity.perspectiveCoordinate(): AnchoredCoordinate {
    return AnchoredCoordinate.fromFrontLeftOrthonormal(headLookAngle, headLeftVector()).anchor(eyePosition)
}

/**
 * Calculates the normalized `front` and `left` perspective vectors as if the entity
 * were facing towards [towards], without modifying the entity's actual rotation.
 *
 * @param towards Target world position or direction vector.
 * @param isAbsolutePosition Set to `true` if [towards] is a world position (default),
 * or `false` if [towards] is already a direction vector.
 * @param then Lambda receiving the normalized (front, left) vectors.
 */
inline fun Entity.whenFace(
    towards: Vec3,
    isAbsolutePosition: Boolean,
    then: (front: Vec3, left: Vec3) -> Unit
) {
    // 1. Calculate direction vector from eye position to target
    val dir = if (isAbsolutePosition) towards.subtract(this.eyePosition) else towards
    val front = dir.normalize()

    // 2. Derive horizontal component length squared (X^2 + Z^2)
    val hLenSqr = front.x * front.x + front.z * front.z

    // 3. Compute pitch-invariant horizontal Left vector
    val left = if (hLenSqr > 1e-7) {
        val hLen = sqrt(hLenSqr)
        // Cross product of World Up (0, 1, 0) and horizontal Front (Fx, 0, Fz) -> (Fz, 0, -Fx)
        Vec3(front.z / hLen, 0.0, -front.x / hLen)
    } else {
        // Fallback for pitch dead-zones (+90° / -90° looking straight UP/DOWN):
        // Retain entity's current head Yaw
        val yawRad = Math.toRadians(this.getViewYRot(1.0f).toDouble())
        Vec3(cos(yawRad), 0.0, sin(yawRad))
    }

    then(front, left)
}
