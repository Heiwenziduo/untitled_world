package com.github.nahnullscience.cypher_nexus.content.entity

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.SPAWN_EGG
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DedicatedCypherProjectile
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.ItemSupplier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

class SpawnEgg(
    entityType: EntityType<out DedicatedCypherProjectile>,
    level: Level
) : DedicatedCypherProjectile(entityType, level), ItemSupplier {
    override val cypherHolder = SPAWN_EGG
    override fun getItem() = ItemStack(Items.EGG)

    override fun discardVisualEffect() {
        for (i in 0..7) {
            level().addParticle(ItemParticleOption(ParticleTypes.ITEM, Items.EGG), x, y, z, 0.0, 0.0, 0.0)
        }
    }
}