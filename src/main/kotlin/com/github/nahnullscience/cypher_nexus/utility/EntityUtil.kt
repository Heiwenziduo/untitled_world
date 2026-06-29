package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity

object EntityUtil {
    /** check if A & B is adversarial  */
    fun isEnemy(A: LivingEntity, B: LivingEntity) : Boolean {
        if (A.`is`(B)) return false
        if (!A.isAttackable || !B.isAttackable) return false
//        if (A.isInvulnerable)
        return true
    }

    /** a direct duplication of #updateRotation in Projectile */
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

    fun lerpRotation(from: Float, to: Float, factor: Float): Float {
        var rotO = from
        while (to - rotO < -180.0f) {
            rotO -= 360.0f
        }

        while (to - rotO >= 180.0f) {
            rotO += 360.0f
        }

        return Mth.lerp(factor, rotO, to)
    }
}