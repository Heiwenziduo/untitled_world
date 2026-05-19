package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.world.entity.LivingEntity

object LivingUtility {
    /** check if A & B is adversarial  */
    fun isEnemy(A: LivingEntity, B: LivingEntity) : Boolean {
        if (A.`is`(B)) return false
        if (!A.isAttackable || !B.isAttackable) return false
//        if (A.isInvulnerable)
        return true
    }
}