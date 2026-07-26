package com.github.nahnullscience.cypher_nexus.client.renderer.state

import com.mojang.blaze3d.systems.RenderPass
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.SubmitNodeCollector.ParticleGroupRenderer
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer.ParticleBufferCache
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState
import net.minecraft.client.renderer.state.level.QuadParticleRenderState.PreparedBuffers
import net.minecraft.client.renderer.texture.TextureManager

class CypherTrailParticleRenderState : ParticleGroupRenderState, ParticleGroupRenderer {
    var particleCount: Int = 0
        private set

    override fun submit(
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
        if (this.particleCount > 0) {
//            submitNodeCollector.submitParticleGroup(this)
            submitNodeCollector
        }
    }

    override fun isEmpty(): Boolean {
        return particleCount == 0
    }

    override fun prepare(
        buffer: ParticleBufferCache,
        translucent: Boolean
    ): PreparedBuffers? {
        TODO("Not yet implemented")
    }

    override fun render(
        buffers: PreparedBuffers,
        bufferCache: ParticleBufferCache,
        renderPass: RenderPass,
        textureManager: TextureManager
    ) {
        TODO("Not yet implemented")
    }
}