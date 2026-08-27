package com.github.nahnullscience.cypher_nexus.content.entity.projectile

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

class Arrow(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : AbstractDedicatedCypherProjectile(entityType, level) {
    override val cypherHolder = Cyphers.ARROW

    var shakeTime: Float = 0f
        private set

    override fun getRotationSpeed(): Float = 0.25f

    override fun discardVisualEffect() {
//        for (i in 0 until radiusFriendlyParticleCount(8, 64)) {
//            level().addParticle(ItemParticleOption(ParticleTypes.ITEM, Items.ARROW), x, y, z, 0.0, 0.0, 0.0)
//        }
    }
}
