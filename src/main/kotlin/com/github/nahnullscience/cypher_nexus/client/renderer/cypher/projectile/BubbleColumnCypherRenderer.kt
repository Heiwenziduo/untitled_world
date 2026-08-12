package com.github.nahnullscience.cypher_nexus.client.renderer.cypher.projectile

import com.github.nahnullscience.cypher_nexus.client.particle.addCypherTrailParticle
import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.SimpleParticleProjectileRenderer
import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.ParticleProjectileRenderState
import com.github.nahnullscience.cypher_nexus.content.entity.projectile.BubbleColumn
import com.github.nahnullscience.cypher_nexus.utility.linearInterpolateGaps
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.core.particles.ParticleTypes

class BubbleColumnCypherRenderer(context: Context) : SimpleParticleProjectileRenderer<BubbleColumn>(context) {

    override fun addTrailParticles(
        level: ClientLevel,
        ce: BubbleColumn,
        x: Double,
        y: Double,
        z: Double,
        xo: Double,
        yo: Double,
        zo: Double
    ) {
        linearInterpolateGaps(xo, yo, zo, x, y, z, 0.25) { step, x, y, z ->
            addCypherTrailParticle(ce, ParticleTypes.BUBBLE, x, y, z, 0.0, 0.0, 0.0)
        }
    }

    override fun submit(
        state: ParticleProjectileRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
        // TODO draw particle
//        poseStack.pushPose()
//        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lightning()) { pose, buffer ->
//
//        }
//        poseStack.popPose()
        super.submit(state, poseStack, submitNodeCollector, camera)
    }
}