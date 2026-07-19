package com.github.nahnullscience.cypher_nexus.content.entity

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.SNOWBALL
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.ItemSupplier
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

class Snowball(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : AbstractDedicatedCypherProjectile(entityType, level), ItemSupplier {
    override val cypherHolder = SNOWBALL
    override fun getItem() = Items.SNOWBALL.defaultInstance

    override fun discardVisualEffect() {
        for (i in 0 until radiusFriendlyParticleCount(8, 64)) {
            level().addParticle(ParticleTypes.ITEM_SNOWBALL, x, y, z, 0.0, 0.0, 0.0)
        }
    }
}
