package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f

object EntityUtil {
    /** check if A & B is adversarial  */
    fun isEnemy(A: LivingEntity, B: LivingEntity) : Boolean {
        if (A.`is`(B)) return false
        if (!A.isAttackable || !B.isAttackable) return false
//        if (A.isInvulnerable)
        return true
    }
}
/**
 * a direct duplication of #updateRotation in Projectile
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
 * get `normalized` vector pointing upward from the top of the entity cranium
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