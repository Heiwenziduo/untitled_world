package com.github.nahnullscience.cypher_nexus.client.renderer.cypher

import com.github.nahnullscience.cypher_nexus.client.particle.CypherTrailParticleGroup.Companion.addCypherTrailParticle
import com.github.nahnullscience.cypher_nexus.content.entity.SmokeBomb
import com.github.nahnullscience.cypher_nexus.utility.linearInterpolateGaps
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.core.particles.ParticleTypes

class SmokeBombCypherRenderer(context: Context) : SimpleParticleProjectileRenderer<SmokeBomb>(context) {

    override fun clientTickPost(
        level: ClientLevel,
        entity: SmokeBomb,
        x: Double,
        y: Double,
        z: Double,
        xo: Double,
        yo: Double,
        zo: Double
    ) {
        linearInterpolateGaps(xo, yo, zo, x, y, z, 0.25) { step, x, y, z ->
            addCypherTrailParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0.0, 0.0, 0.0) {
                entity.hueFloatArray?.let {
                    setColor(it[0], it[1], it[2])
                    setAlpha(it[3])
                }
                scale(entity.getEffectRadius().coerceIn(0.5f, 4.0f))
                lifetime += 60
            }
        }
    }
}