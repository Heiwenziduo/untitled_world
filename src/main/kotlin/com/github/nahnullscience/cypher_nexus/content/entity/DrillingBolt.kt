package com.github.nahnullscience.cypher_nexus.content.entity

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.utility.mostAlignedDirection
import net.minecraft.core.Direction
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

class DrillingBolt(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : AbstractDedicatedCypherProjectile(entityType, level) {
    override val cypherHolder = Cyphers.DRILLING_BOLT

    override fun beforeDiscard(reason: DiscardReason) {
        var pos = blockPosition()
        var block = level().getBlockState(pos)
        if (block.isAir) {
            val d = deltaMovement.mostAlignedDirection()
            pos = pos.relative(d)
            block = level().getBlockState(pos)
        }
        if (!block.isEmpty) {
            println("$block is not empty")
        }
        val id = owner()?.id ?: id
        level().destroyBlockProgress(id, pos, 8)
    }
}