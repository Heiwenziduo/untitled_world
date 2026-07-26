package com.github.nahnullscience.cypher_nexus.client.particle

import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.SpriteSet
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.util.RandomSource

class DistanceInvokeTrail(
    level: ClientLevel,
    x: Double, y: Double, z: Double,
    xa: Double, ya: Double, za: Double
) : CypherTrailParticle(level, x, y, z, xa, ya, za) {

    class TrailProvider(val spriteSet: SpriteSet) : ParticleProvider<SimpleParticleType> {
        override fun createParticle(
            options: SimpleParticleType,
            level: ClientLevel,
            x: Double,
            y: Double,
            z: Double,
            xAux: Double,
            yAux: Double,
            zAux: Double,
            random: RandomSource
        ): Particle {
            return DistanceInvokeTrail(level, x, y, z, xAux, yAux, zAux)
        }
    }
}