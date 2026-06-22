package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile.Companion.CAPTURE_SIZE_SQR
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity

class HooksSharedData {
    var homingTarget: Entity? = null
    var pathDirection0: Direction? = null
    var pathDirection: Direction? = null

    fun tick(projectile: AbstractCypherProjectile) {

        // clear target if too far
        homingTarget?.let {
            if (projectile.distanceToSqr(it.eyePosition) > CAPTURE_SIZE_SQR * 2)
                homingTarget = null
        }

        pathDirection0 = pathDirection
        pathDirection = null
    }
}