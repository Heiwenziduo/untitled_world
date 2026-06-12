package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile.Companion.CAPTURE_SIZE_SQR
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothEntitySearchHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothTickBehaviorHook
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

object HomingCypher: ModifierCypher(), BothTickBehaviorHook, BothEntitySearchHook {
    const val HOMING_STRENGTH = 0.1
    override val resource = CypherNexus.modResource("homing")

    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(60f)
    }

    override fun tickBehaviorBoth(
        level: Level,
        projectile: AbstractCypherProjectile,
        strength: Int
    ) {
        val target = projectile.hookData.homingTarget ?: return
        if (projectile.distanceToSqr(target.eyePosition) > CAPTURE_SIZE_SQR * 2) {
            projectile.hookData.homingTarget = null
            return
        }
        if (!target.boundingBox.contains(projectile.position())) {
            val speed = projectile.position().vectorTo(target.eyePosition).normalize().scale(strength * HOMING_STRENGTH)
            projectile.deltaMovement = projectile.deltaMovement.add(speed)
        }
    }

    override fun entitySearchBoth(
        level: Level,
        projectile: AbstractCypherProjectile,
        strength: Int,
        target: Entity
    ) {
        if (projectile.canHomeTarget(target)) {
            if (projectile.hookData.homingTarget == null)
                projectile.hookData.homingTarget = target
            else {
                val old = projectile.position().distanceToSqr(projectile.hookData.homingTarget!!.eyePosition)
                val new = projectile.position().distanceToSqr(target.eyePosition)
                if (new < old) projectile.hookData.homingTarget = target
            }
        }
    }

    override fun needSearch(
        level: Level,
        projectile: AbstractCypherProjectile
    ): Boolean {
        val target = projectile.hookData.homingTarget ?: return true
        return !projectile.canHomeTarget(target)
    }

}