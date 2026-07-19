package com.github.nahnullscience.cypher_nexus.content.entity

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.ENDER_TELEPORTATION
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.ItemSupplier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.portal.TeleportTransition

open class EnderTeleportation(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : AbstractDedicatedCypherProjectile(entityType, level), ItemSupplier {
    companion object {
        // teleportation utils

    }

    override val cypherHolder = ENDER_TELEPORTATION
    override fun getItem() = ItemStack(Items.ENDER_PEARL)

    override fun discardVisualEffect() {
//        for (i in 0..7) {
//            level().addParticle(
////                ColorParticleOption.create(ParticleTypes.DRAGON_BREATH, 114f, 51f, 4f),
//                PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1f),
//                x, y, z, 0.0, -0.1, 0.0)
//        }

        for (i in 0..31) {
            level()
                .addParticle(
                    ParticleTypes.PORTAL,
                    x,
                    y + random.nextDouble() * 2.0,
                    z,
                    random.nextGaussian(),
                    0.0,
                    random.nextGaussian()
                )
        }
    }

    override fun beforeDiscard(reason: DiscardReason) {
        if (!level().isClientSide && owner() != null) {
            // compare to #teleportTo on Entity, this can handle dimension
            // owner()?.teleportTo(x, y, z)
            val owner = owner()!!
            owner.teleport(TeleportTransition(
                level() as ServerLevel,
                position(),
                owner.deltaMovement,
                owner.yRot,
                owner.xRot,
                TeleportTransition.DO_NOTHING
            )).let { newOwner ->
                newOwner?.resetFallDistance()
                if (newOwner is LivingEntity) newOwner.resetCurrentImpulseContext()
            }

            level().playSound(null, x, y, z, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS)
        }


        super.beforeDiscard(reason)
    }
}