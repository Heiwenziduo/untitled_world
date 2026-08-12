package com.github.nahnullscience.cypher_nexus.content.entity.projectile

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.ENDER_TELEPORTATION
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.utility.isServerSide
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.level.portal.TeleportTransition

open class EnderTeleportation(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : AbstractDedicatedCypherProjectile(entityType, level) {
    override val cypherHolder = ENDER_TELEPORTATION

    override fun discardVisualEffect() {
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




    override fun <CE> beforeDiscardServer(
        ce: CE,
        level: ServerLevel,
        reason: DiscardReason
    ) where CE : Entity, CE : ICypherEntity {
        owner()?.let { owner ->
            // compare to #teleportTo on Entity, this can handle dimension
            // owner()?.teleportTo(x, y, z)
            owner.teleport(TeleportTransition(
                level,
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
        super.beforeDiscardServer(ce, level, reason)
    }
}