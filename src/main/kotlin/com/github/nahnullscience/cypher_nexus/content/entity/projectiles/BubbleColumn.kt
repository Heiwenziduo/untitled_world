package com.github.nahnullscience.cypher_nexus.content.entity.projectiles

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.BUBBLE_COLUMN
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getEffectRadius
import net.minecraft.core.Direction
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult

class BubbleColumn(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : AbstractDedicatedCypherProjectile(entityType, level) {
    override val cypherHolder = BUBBLE_COLUMN

    override fun getUnderwaterSpeedFactor() = 1.25
    override fun discardVisualEffect() {
//        for (i in 0 until radiusFriendlyParticleCount(8, 32)) {
//            level().addParticle(ParticleTypes.BUBBLE, x, y, z, 0.0, 0.0, 0.0)
//        }
    }

    override fun whenHitEntity(result: EntityHitResult, direction: Direction) {
        if (!level().isClientSide) {
            result.entity.let { target ->
                val magnitude = (30 * getEffectRadius()).toInt()
                // air inject
                target.airSupply = (target.airSupply + magnitude).coerceAtMost(target.maxAirSupply)
            }
        }
        level().playLocalSound(
            result.location.x,
            result.location.y,
            result.location.z,
            SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE,
            soundSource,
            1.0f,
            1.0f,
            false
        )
    }
}