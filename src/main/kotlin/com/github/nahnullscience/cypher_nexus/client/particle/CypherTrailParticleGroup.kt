package com.github.nahnullscience.cypher_nexus.client.particle

import com.github.nahnullscience.cypher_nexus.CypherNexus.MOD_ID
import com.github.nahnullscience.cypher_nexus.client.renderer.state.CypherTrailParticleRenderState
import com.github.nahnullscience.cypher_nexus.init.config.ModClientConfig
import net.minecraft.CrashReport
import net.minecraft.CrashReportDetail
import net.minecraft.ReportedException
import net.minecraft.client.Camera
import net.minecraft.client.particle.ParticleEngine
import net.minecraft.client.particle.ParticleGroup
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState

class CypherTrailParticleGroup(
    engine: ParticleEngine
) : ParticleGroup<CypherTrailParticle>(engine) {
    companion object {
        val CYPHER_TRAIL_RENDER_TYPE: ParticleRenderType = ParticleRenderType("$MOD_ID:cypher_trail")
    }

    val maxParticles = ModClientConfig.CONFIG.maxTrailParticleCount.asInt

    val renderType: ParticleRenderType? = null
    val renderState = CypherTrailParticleRenderState()


    override fun extractRenderState(
        frustum: Frustum,
        camera: Camera,
        partialTickTime: Float
    ): ParticleGroupRenderState {
        for (p in particles) {
            if (frustum.pointInFrustum(p.getX(), p.getY(), p.getZ())) {
                try {
                    p.extract(renderState, camera, partialTickTime)
                } catch (var9: Throwable) {
                    val report = CrashReport.forThrowable(var9, "Rendering Particle")
                    val category = report.addCategory("Particle being rendered")
                    category.setDetail("Particle", CrashReportDetail { p.toString() })
                    category.setDetail("Particle Type", CrashReportDetail { renderType.toString() })
                    throw ReportedException(report)
                }
            }
        }
        return renderState
    }
}