package com.github.nahnullscience.cypher_nexus.client.particle

import com.github.nahnullscience.cypher_nexus.CypherNexus.MOD_ID
import com.github.nahnullscience.cypher_nexus.init.config.ModClientConfig
import com.google.common.collect.EvictingQueue
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.ParticleEngine
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.particle.QuadParticleGroup
import net.minecraft.client.particle.SingleQuadParticle
import net.minecraft.core.particles.ParticleOptions
import java.util.*

class CypherTrailParticleGroup(
    engine: ParticleEngine
) : QuadParticleGroup(engine, CYPHER_TRAIL_RENDER_TYPE) {
    companion object {
        val CYPHER_TRAIL_RENDER_TYPE: ParticleRenderType = ParticleRenderType("$MOD_ID:cypher_trail")

        const val MAX_PARTICLE_TRACKING_DISTANCE_SQR = 128.0 * 128.0

        val particleEngine get() = Minecraft.getInstance().particleEngine

        val mainCamera get() = Minecraft.getInstance().gameRenderer.mainCamera

        @PublishedApi
        internal var INSTANCE: CypherTrailParticleGroup? = null

        @PublishedApi
        internal fun updateInstance(): CypherTrailParticleGroup {
            particleEngine.let { engine ->
                return (engine.particles.computeIfAbsent(CYPHER_TRAIL_RENDER_TYPE) { type ->
                    CypherTrailParticleGroup(engine)
                } as CypherTrailParticleGroup).also { INSTANCE = it }
            }
        }

        /**
         *
         * */
        // now we can ntr vanilla quad to our render layer
        fun <T : ParticleOptions> addCypherTrailParticle(
            options: T,
            x: Double, y: Double, z: Double,
            xa: Double, ya: Double, za: Double
        ) {
            if (mainCamera.position().distanceToSqr(x, y, z) > MAX_PARTICLE_TRACKING_DISTANCE_SQR) return
            val particle = particleEngine.makeParticle(options, x, y, z, xa, ya, za)
            if (particle is SingleQuadParticle) {
                (INSTANCE ?: updateInstance()).particlesToAdd.add(particle)
            }
        }
        /**
         *
         * */
        inline fun <T : ParticleOptions> addCypherTrailParticle(
            options: T,
            x: Double, y: Double, z: Double,
            xa: Double, ya: Double, za: Double,
            config: SingleQuadParticle.() -> Unit
        ) {
            if (mainCamera.position().distanceToSqr(x, y, z) > MAX_PARTICLE_TRACKING_DISTANCE_SQR) return
            val particle = Minecraft.getInstance().particleEngine.makeParticle(options, x, y, z, xa, ya, za)
            if (particle is SingleQuadParticle) {
                particle.config()
                (INSTANCE ?: updateInstance()).particlesToAdd.add(particle)
            }
        }

        fun SingleQuadParticle.setARGB(a: Float, r: Float, g: Float, b: Float) {
            setColor(r, g, b)
            setAlpha(a)
        }
    }

    val maxCount = ModClientConfig.CONFIG.maxTrailParticleCount.asInt
    @PublishedApi
    internal val particlesToAdd: Queue<SingleQuadParticle> = EvictingQueue.create(maxCount)
    init {
        particles = EvictingQueue.create(maxCount)
    }

    override fun tickParticles() {
        super.tickParticles()
        if (particlesToAdd.isNotEmpty()) {
            var v = particlesToAdd.poll()
            while (v != null) {
                particles.add(v)
                v = particlesToAdd.poll()
            }
        }
    }
}