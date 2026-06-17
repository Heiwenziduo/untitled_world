package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile.Companion.CAPTURE_SIZE_SQR
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothEntitySearchHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothTickBehaviorHook
import com.github.nahnullscience.cypher_nexus.utility.rotateTowards
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import kotlin.math.PI
import kotlin.math.min

/** these homing-s share the same "homing target" on HooksSharedData */
abstract class AbstractHoming(path: String, private val _manaDrain: Float): ModifierCypher(), BothTickBehaviorHook, BothEntitySearchHook {

    companion object {
        const val HOMING_STRENGTH = 0.1
        const val ROTATION_RADIUS = PI / 36
    }

    override val resource = CypherNexus.modResource(path)

    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(_manaDrain)
    }

    override fun entitySearchBoth(
        level: Level,
        projectile: AbstractCypherProjectile,
        strength: Int,
        target: Entity
    ) {
        if (projectile.hooksSharedData.homingTarget == target) return
        if (projectile.canHomeTarget(target)) {
            if (projectile.hooksSharedData.homingTarget == null)
                projectile.hooksSharedData.homingTarget = target
            else {
                val old = projectile.position().distanceToSqr(projectile.hooksSharedData.homingTarget!!.eyePosition)
                val new = projectile.position().distanceToSqr(target.eyePosition)
                if (new < old) projectile.hooksSharedData.homingTarget = target
            }
        }
    }

    override fun needSearch(
        level: Level,
        projectile: AbstractCypherProjectile
    ): Boolean {
        val target = projectile.hooksSharedData.homingTarget ?: return true
        return !projectile.canHomeTarget(target)
    }

    protected open fun authenticateTarget(projectile: AbstractCypherProjectile): Boolean {
        // consider merge with AbstractCypherProjectile#canHomeTarget
        val target = projectile.hooksSharedData.homingTarget ?: return false
        if (projectile.distanceToSqr(target.eyePosition) > CAPTURE_SIZE_SQR * 2) {
            projectile.hooksSharedData.homingTarget = null
            return false
        }
        return true
    }


    object Homing: AbstractHoming("homing", 60f) {
        override fun tickBehaviorBoth(
            level: Level,
            projectile: AbstractCypherProjectile,
            strength: Int
        ) {
            if (authenticateTarget(projectile)) {
                val target = projectile.hooksSharedData.homingTarget!!
                if (!target.boundingBox.contains(projectile.position())) {
                    val dir = projectile.position().vectorTo(target.eyePosition)
                    val dis =  min(dir.length(), strength * HOMING_STRENGTH)
                    val speed = dir.normalize().scale(dis)
                    projectile.push(speed)
                }
            }
        }
    }

    object TurnToTarget: AbstractHoming("turn_toward_target", 30f) {
        override fun tickBehaviorBoth(
            level: Level,
            projectile: AbstractCypherProjectile,
            strength: Int
        ) {
            if (authenticateTarget(projectile)) {
                val target = projectile.hooksSharedData.homingTarget!!
                if (!target.boundingBox.contains(projectile.position())) {
                    val dir = projectile.position().vectorTo(target.eyePosition)
                    projectile.deltaMovement = projectile.deltaMovement.rotateTowards(dir, ROTATION_RADIUS * strength)
                }
            }
        }
    }
}