package com.github.nahnullscience.cypher_nexus.client.renderer.cypher.utility

import com.github.nahnullscience.cypher_nexus.client.util.addCypherTrailParticle
import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.AbstractCypherRenderer
import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.utility.DistanceDeliverCypherRenderer.DistanceDeliverCypherRenderState
import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component.CypherRenderStateDelegate
import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component.ICypherEntityRenderState
import com.github.nahnullscience.cypher_nexus.content.entity.utility.DistanceDeliverer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.BouncePointsManager.Companion.forEachGap
import com.github.nahnullscience.cypher_nexus.utility.forEachGap
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.core.particles.ParticleTypes

class DistanceDeliverCypherRenderer (
    context: Context
) : AbstractCypherRenderer<DistanceDeliverer, DistanceDeliverCypherRenderState>(context) {

    override fun submit(
        state: DistanceDeliverCypherRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
        super.submit(state, poseStack, submitNodeCollector, camera)
    }

    override fun addTrailParticles(
        level: ClientLevel,
        ce: DistanceDeliverer,
        x: Double, y: Double, z: Double,
        xo: Double, yo: Double, zo: Double
    ) {
        forEachGap(
            xo, yo, zo,
            x, y, z,
            0.25,
            ce.bouncePoints
        ) { step, x, y, z ->
            addCypherTrailParticle(
                ce,
                ParticleTypes.PORTAL,
                x, y - 0.5, z,
            ) {
                lifetime = 10 + step
                scale(0.25f)
            }
        }
    }

    override fun createRenderState() = DistanceDeliverCypherRenderState()

    class DistanceDeliverCypherRenderState: EntityRenderState(), ICypherEntityRenderState by CypherRenderStateDelegate()
}
