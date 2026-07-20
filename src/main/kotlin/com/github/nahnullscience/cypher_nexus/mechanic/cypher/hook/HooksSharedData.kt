package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity.Companion.CAPTURE_SIZE_SQR
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity

class HooksSharedData <CY> where CY : Entity, CY : ICypherEntity {
    var homingTarget: Entity? = null
    var pathDirection0: Direction? = null
    var pathDirection: Direction? = null
    var chaoticPathTick: Int = 7

    fun tick(entity: CY) {

        // clear target if too far
        homingTarget?.let {
            if (it.isRemoved || entity.distanceToSqr(it.eyePosition) > CAPTURE_SIZE_SQR * 2)
                homingTarget = null
        }

        pathDirection0 = pathDirection
        pathDirection = null
    }
}