package com.github.nahnullscience.cypher_nexus.content.entity

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.BUBBLE_COLUMN
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DedicatedCypherProjectile
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

class BubbleColumn(
    entityType: EntityType<out DedicatedCypherProjectile>,
    level: Level
) : DedicatedCypherProjectile(entityType, level) {
    override val cypherHolder = BUBBLE_COLUMN

    override fun getUnderwaterSpeedFactor() = 1.05f
    override fun discardVisualEffect() {
        for (i in 0 until radiusFriendlyParticleCount(8, 32)) {
            level().addParticle(ParticleTypes.BUBBLE, x, y, z, 0.0, 0.0, 0.0)
        }
    }
}