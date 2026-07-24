package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity.Companion.CAPTURE_SIZE_SQR
import com.github.nahnullscience.cypher_nexus.utility.mod.CircleDefinition
import net.minecraft.world.entity.Entity

class HooksSharedData <CE> where CE : Entity, CE : ICypherEntity {
    var homingTarget: Entity? = null
//    var pathDirection0: Direction? = null
//    var pathDirection: Direction? = null
    var chaoticPathTick: Int = 7
    var orbitingCircle: CircleDefinition? = null

    fun tick(entity: CE) {

        // clear target if too far
        homingTarget?.let {
            if (it.isRemoved || entity.distanceToSqr(it.eyePosition) > CAPTURE_SIZE_SQR * 2)
                homingTarget = null
        }

//        pathDirection0 = pathDirection
//        pathDirection = null
    }
}