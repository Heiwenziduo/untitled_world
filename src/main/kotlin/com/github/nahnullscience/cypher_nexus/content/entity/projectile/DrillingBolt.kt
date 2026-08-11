package com.github.nahnullscience.cypher_nexus.content.entity.projectile

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.utility.mostAlignedDirection
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult

class DrillingBolt(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : AbstractDedicatedCypherProjectile(entityType, level) {
    override val cypherHolder = Cyphers.DRILLING_BOLT

    override fun <CE> beforeDiscardServer(ce: CE, reason: DiscardReason) where CE : Entity, CE : ICypherEntity {
//        var pos = blockPosition()
//        var block = level().getBlockState(pos)
//        if (block.isAir) {
//            val d = deltaMovement.mostAlignedDirection()
//            pos = pos.relative(d)
//            block = level().getBlockState(pos)
//        }
//        if (!block.isEmpty) {
//            println("$block is not empty")
//        }
//        val id = owner()?.id ?: id
//        level().destroyBlockProgress(id, pos, 8)
        super.beforeDiscardServer(ce, reason)
    }

    override fun whenHitBlock(result: BlockHitResult, direction: Direction) {
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