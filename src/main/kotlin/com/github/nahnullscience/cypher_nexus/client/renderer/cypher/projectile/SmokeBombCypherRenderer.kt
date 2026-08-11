package com.github.nahnullscience.cypher_nexus.client.renderer.cypher.projectile

import com.github.nahnullscience.cypher_nexus.client.particle.addCypherTrailParticle
import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.SimpleParticleProjectileRenderer
import com.github.nahnullscience.cypher_nexus.content.entity.projectile.SmokeBomb
import com.github.nahnullscience.cypher_nexus.utility.linearInterpolateGaps
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.core.particles.ParticleTypes

class SmokeBombCypherRenderer(context: Context) : SimpleParticleProjectileRenderer<SmokeBomb>(context) {

    override fun addTrailParticles(
        level: ClientLevel,
        entity: SmokeBomb,
        x: Double,
        y: Double,
        z: Double,
        xo: Double,
        yo: Double,
        zo: Double
    ) {
        linearInterpolateGaps(xo, yo, zo, x, y, z, 0.35) { step, x, y, z ->
            addCypherTrailParticle(entity, ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0.0, 0.0, 0.0) {
                lifetime += 60
            }
        }
    }
}