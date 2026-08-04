package com.github.nahnullscience.cypher_nexus.content.entity

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.LLAMA_SPIT
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

class LlamaSpit(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : AbstractDedicatedCypherProjectile(entityType, level) {
    override val cypherHolder = LLAMA_SPIT

    override fun recreateFromPacket(packet: ClientboundAddEntityPacket) {
        super.recreateFromPacket(packet)

        val movement = packet.movement
        for (i in 0..6) {
            val k = 0.4 + 0.1 * i
            this.level().addParticle(
                ParticleTypes.SPIT,
                this.x,
                this.y,
                this.z,
                movement.x * k,
                movement.y,
                movement.z * k
            )
        }
    }
}