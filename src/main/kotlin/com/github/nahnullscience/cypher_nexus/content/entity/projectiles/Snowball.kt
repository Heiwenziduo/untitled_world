package com.github.nahnullscience.cypher_nexus.content.entity.projectiles

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.SNOWBALL
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

class Snowball(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : AbstractDedicatedCypherProjectile(entityType, level) {
    override val cypherHolder = SNOWBALL

    override fun discardVisualEffect() {
        for (i in 0 until radiusFriendlyParticleCount(8, 64)) {
            level().addParticle(ParticleTypes.ITEM_SNOWBALL, x, y, z, 0.0, 0.0, 0.0)
        }
    }
}
