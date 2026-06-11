package com.github.nahnullscience.cypher_nexus.content.entity

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.ARROW
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

class Arrow(
    entityType: EntityType<out AbstractCypherProjectile>,
    level: Level
) : AbstractCypherProjectile(entityType, level) {
    override val cypherHolder = ARROW

    override fun discardVisualEffect() {
        for (i in 0..7) {
            level().addParticle(ItemParticleOption(ParticleTypes.ITEM, Items.ARROW), x, y, z, 0.0, 0.0, 0.0)
        }
    }
}