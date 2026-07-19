package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.TickBehaviorHook
import com.github.nahnullscience.cypher_nexus.utility.rotateTowards
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import kotlin.math.PI

class AimingArc(
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
) : ModifierCypher(defaultAttribute), TickBehaviorHook {
    companion object {
        private const val ROTATION_RADIUS = PI / 18
    }

    override val resource = CypherNexus.modResource("aiming_arc")
    override fun <CY> onTick(
        index: Int,
        count: Int,
        level: Level,
        cyEntity: CY
    ) where CY : Entity, CY : ICypherEntity {
        if (cyEntity.tickCount < 8) return
        cyEntity.owner?.let {
            cyEntity.deltaMovement = cyEntity.deltaMovement.rotateTowards(it.headLookAngle, ROTATION_RADIUS * count)
        }
    }
}