package com.github.nahnullscience.cypher_nexus.content.entity

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.ARROW
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DedicatedCypherProjectile
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

class Arrow(
    entityType: EntityType<out DedicatedCypherProjectile>,
    level: Level
) : DedicatedCypherProjectile(entityType, level) {
    override val cypherHolder = ARROW

    var shakeTime: Float = 0f
        private set

    override fun discardVisualEffect() {
        for (i in 0..radiusFriendlyParticleCount(8, 64)) {
            level().addParticle(ItemParticleOption(ParticleTypes.ITEM, Items.ARROW), x, y, z, 0.0, 0.0, 0.0)
        }
    }
}