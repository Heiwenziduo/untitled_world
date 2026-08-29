package com.github.nahnullscience.cypher_nexus.client.util

import com.github.nahnullscience.cypher_nexus.client.particle.CypherTrailParticleGroup.Companion.INSTANCE
import com.github.nahnullscience.cypher_nexus.client.particle.CypherTrailParticleGroup.Companion.MAX_PARTICLE_TRACKING_DISTANCE_SQR
import com.github.nahnullscience.cypher_nexus.client.particle.CypherTrailParticleGroup.Companion.mainCamera
import com.github.nahnullscience.cypher_nexus.client.particle.CypherTrailParticleGroup.Companion.updateInstance
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getEffectRadius
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.SingleQuadParticle
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.world.entity.Entity

// now we can ntr vanilla quad to our render layer

/**
 *
 * */
inline fun <T : ParticleOptions> addCypherTrailParticle(
    options: T,
    x: Double, y: Double, z: Double,
    xa: Double = 0.0, ya: Double = 0.0, za: Double = 0.0,
    crossinline config: SingleQuadParticle.() -> Unit
) {
    if (mainCamera.position().distanceToSqr(x, y, z) > MAX_PARTICLE_TRACKING_DISTANCE_SQR) return
    val particle = Minecraft.getInstance().particleEngine.makeParticle(options, x, y, z, xa, ya, za)
    if (particle is SingleQuadParticle) {
        particle.config()
        (INSTANCE ?: updateInstance()).particlesToAdd.add(particle)
    }
}

/**
 * with basic setup
 * */
inline fun <T : ParticleOptions, CE> addCypherTrailParticle(
    cyEntity: CE,
    options: T,
    x: Double, y: Double, z: Double,
    xa: Double = 0.0, ya: Double = 0.0, za: Double = 0.0,
    crossinline config: SingleQuadParticle.() -> Unit
) where CE : Entity, CE : ICypherEntity {
    addCypherTrailParticle(options, x, y, z, xa, ya, za) {
        scale(cyEntity.getEffectRadius().coerceIn(0.25f, 4f))
        if (cyEntity.dyed) cyEntity.hueFloatArray.let {
            setColor(it[0], it[1], it[2])
            setAlpha(it[3]) // TODO if no alpha pass
        }
        config()
    }
}

/**
 *
 * */
fun <T : ParticleOptions, CE> addCypherTrailParticle(
    cyEntity: CE,
    options: T,
    x: Double, y: Double, z: Double,
    xa: Double = 0.0, ya: Double = 0.0, za: Double = 0.0,
) where CE : Entity, CE : ICypherEntity = addCypherTrailParticle(cyEntity, options, x, y, z, xa, ya, za) { }
