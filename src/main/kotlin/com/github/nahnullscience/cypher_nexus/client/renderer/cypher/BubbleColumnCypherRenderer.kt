package com.github.nahnullscience.cypher_nexus.client.renderer.cypher

import com.github.nahnullscience.cypher_nexus.client.particle.CypherTrailParticleGroup.Companion.addCypherTrailParticle
import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.ParticleProjectileRenderState
import com.github.nahnullscience.cypher_nexus.content.entity.BubbleColumn
import com.github.nahnullscience.cypher_nexus.utility.linearInterpolateGaps
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.core.particles.ParticleTypes

class BubbleColumnCypherRenderer(context: Context) : SimpleParticleProjectileRenderer<BubbleColumn>(context) {

    override fun clientTickPost(
        level: ClientLevel,
        entity: BubbleColumn,
        x: Double,
        y: Double,
        z: Double,
        xo: Double,
        yo: Double,
        zo: Double
    ) {
        linearInterpolateGaps(xo, yo, zo, x, y, z, 0.25) { step, x, y, z ->
            addCypherTrailParticle(ParticleTypes.BUBBLE, x, y, z, 0.0, 0.0, 0.0) {
                entity.hueFloatArray?.let {
                    setColor(it[0], it[1], it[2])
                    setAlpha(it[3])
                }
                scale(entity.getEffectRadius().coerceIn(0.25f, 4.0f))
            }
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