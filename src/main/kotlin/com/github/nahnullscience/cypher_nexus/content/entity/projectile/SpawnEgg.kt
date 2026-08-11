package com.github.nahnullscience.cypher_nexus.content.entity.projectile

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.SPAWN_EGG
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

class SpawnEgg(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : AbstractDedicatedCypherProjectile(entityType, level) {
    override val cypherHolder = SPAWN_EGG

    override fun discardVisualEffect() {
        for (i in 0 until radiusFriendlyParticleCount(8, 32)) {
            level().addParticle(ItemParticleOption(ParticleTypes.ITEM, Items.EGG), x, y, z, 0.0, 0.0, 0.0)
        }
    }
}