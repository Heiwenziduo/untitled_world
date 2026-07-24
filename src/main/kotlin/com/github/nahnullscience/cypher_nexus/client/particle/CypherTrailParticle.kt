package com.github.nahnullscience.cypher_nexus.client.particle

import com.github.nahnullscience.cypher_nexus.client.particle.CypherTrailParticleGroup.Companion.CYPHER_TRAIL
import com.github.nahnullscience.cypher_nexus.client.renderer.state.CypherTrailParticleRenderState
import net.minecraft.client.Camera
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.renderer.texture.TextureAtlasSprite

open class CypherTrailParticle(
    level: ClientLevel,
    x: Double,
    y: Double,
    z: Double,
    xa: Double,
    ya: Double,
    za: Double,
    protected val sprite: TextureAtlasSprite
) : Particle(level, x, y, z, xa, ya, za) {

    fun getX() = x
    fun getY() = y
    fun getZ() = z

    override fun getGroup(): ParticleRenderType = CYPHER_TRAIL

    fun extract(state: CypherTrailParticleRenderState, camera: Camera, partialTickTime: Float) {

    }
}