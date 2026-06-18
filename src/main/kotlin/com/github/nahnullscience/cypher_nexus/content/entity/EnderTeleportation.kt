package com.github.nahnullscience.cypher_nexus.content.entity

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.ENDER_TELEPORTATION
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.particles.PowerParticleOption
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.ItemSupplier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

open class EnderTeleportation(
    entityType: EntityType<out AbstractCypherProjectile>,
    level: Level
) : AbstractCypherProjectile(entityType, level), ItemSupplier {
    override val cypherHolder = ENDER_TELEPORTATION
    override fun getItem() = ItemStack(Items.ENDER_PEARL)

    override fun discardVisualEffect() {
        for (i in 0..7) {
            level().addParticle(
//                ColorParticleOption.create(ParticleTypes.DRAGON_BREATH, 114f, 51f, 4f),
                PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1f),
                x, y, z, 0.0, -0.1, 0.0)
        }
    }

    override fun onBeforeDiscardBoth(reason: DiscardReason) {
        if (!level().isClientSide) {
            owner()?.teleportTo(x, y, z)
        }
        super.onBeforeDiscardBoth(reason)
    }
}