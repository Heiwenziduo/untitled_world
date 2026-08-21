package com.github.nahnullscience.cypher_nexus.content.entity.projectile

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
        if (tickStartSpeedSqr > 4.0) {
            // TODO high speed burst
            val baseOffX = this.random.nextGaussian() * 0.05
            val baseOffZ = this.random.nextGaussian() * 0.05
            for (i in 0 until radiusFriendlyParticleCount(8, 32)) {
                val xd: Double = deltaMovement.x * 1.5 + this.random.nextGaussian() * 0.15 + baseOffX
                val zd: Double = deltaMovement.z * 1.5 + this.random.nextGaussian() * 0.15 + baseOffZ
                val yd: Double = deltaMovement.y * 1.5 + this.random.nextDouble() * 0.5
                level().addParticle(ParticleTypes.ITEM_SNOWBALL, x, y, z, xd, yd, zd)
            }
        } else {
            for (i in 0 until radiusFriendlyParticleCount(8, 32)) {
                level().addParticle(ParticleTypes.ITEM_SNOWBALL, x, y, z, 0.0, 0.0, 0.0)
            }
        }
    }
}
