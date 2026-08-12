package com.github.nahnullscience.cypher_nexus.client.renderer.cypher.projectile

import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.AbstractCypherRenderer
import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.projectile.LightningBoltCypherRenderer.LightningBoltCypherRenderState
import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component.CypherRenderStateDelegate
import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component.ICypherEntityRenderState
import com.github.nahnullscience.cypher_nexus.content.entity.projectile.LightningBolt
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.PoseStack.Pose
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.client.renderer.entity.state.LightningBoltRenderState
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.util.RandomSource
import org.joml.Matrix4fc

class LightningBoltCypherRenderer (
    context: Context
) : AbstractCypherRenderer<LightningBolt, LightningBoltCypherRenderState>(context) {

    override fun submit(
        state: LightningBoltCypherRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
        val xOffs = FloatArray(8)
        val zOffs = FloatArray(8)
        var xOff = 0.0f
        var zOff = 0.0f
        val random = RandomSource.createThreadLocalInstance(state.seed)

        for (h in 7 downTo 0) {
            xOffs[h] = xOff
            zOffs[h] = zOff
            xOff += (random.nextInt(11) - 5).toFloat()
            zOff += (random.nextInt(11) - 5).toFloat()
        }

        val finalXOff = xOff
        val finalZOff = zOff
        submitNodeCollector.submitCustomGeometry(
            poseStack,
            RenderTypes.lightning()
        ) { pose: Pose, buffer: VertexConsumer ->
            val poseMatrix: Matrix4fc = pose.pose()
            for (r in 0..3) {
                val randomx = RandomSource.createThreadLocalInstance(state.seed)

                for (p in 0..2) {
                    var hs = 7
                    var ht = 0
                    if (p > 0) {
                        hs = 7 - p
                    }

                    if (p > 0) {
                        ht = hs - 2
                    }

                    var xo0 = xOffs[hs] - finalXOff
                    var zo0 = zOffs[hs] - finalZOff

                    for (h in hs downTo ht) {
                        val xo1 = xo0
                        val zo1 = zo0
                        if (p == 0) {
                            xo0 += (randomx.nextInt(11) - 5).toFloat()
                            zo0 += (randomx.nextInt(11) - 5).toFloat()
                        } else {
                            xo0 += (randomx.nextInt(31) - 15).toFloat()
                            zo0 += (randomx.nextInt(31) - 15).toFloat()
                        }

                        val br = 0.5f
                        val boltRed = 0.45f
                        val boltGreen = 0.45f
                        val boltBlue = 0.5f
                        var rr1 = 0.1f + r * 0.2f
                        if (p == 0) {
                            rr1 *= h * 0.1f + 1.0f
                        }

                        var rr2 = 0.1f + r * 0.2f
                        if (p == 0) {
                            rr2 *= (h - 1.0f) * 0.1f + 1.0f
                        }

                        lightningQuad(
                            poseMatrix,
                            buffer,
                            xo0,
                            zo0,
                            h,
                            xo1,
                            zo1,
                            0.45f,
                            0.45f,
                            0.5f,
                            rr1,
                            rr2,
                            px1 = false,
                            pz1 = false,
                            px2 = true,
                            pz2 = false
                        )
                        lightningQuad(
                            poseMatrix,
                            buffer,
                            xo0,
                            zo0,
                            h,
                            xo1,
                            zo1,
                            0.45f,
                            0.45f,
                            0.5f,
                            rr1,
                            rr2,
                            px1 = true,
                            pz1 = false,
                            px2 = true,
                            pz2 = true
                        )
                        lightningQuad(
                            poseMatrix,
                            buffer,
                            xo0,
                            zo0,
                            h,
                            xo1,
                            zo1,
                            0.45f,
                            0.45f,
                            0.5f,
                            rr1,
                            rr2,
                            px1 = true,
                            pz1 = true,
                            px2 = false,
                            pz2 = true
                        )
                        lightningQuad(
                            poseMatrix,
                            buffer,
                            xo0,
                            zo0,
                            h,
                            xo1,
                            zo1,
                            0.45f,
                            0.45f,
                            0.5f,
                            rr1,
                            rr2,
                            px1 = false,
                            pz1 = true,
                            px2 = false,
                            pz2 = false
                        )
                    }
                }
            }
        }
    }
    
    override fun createRenderState() = LightningBoltCypherRenderState()
    override fun extractRenderState(entity: LightningBolt, state: LightningBoltCypherRenderState, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
        state.seed = entity.seed
    }

    override fun affectedByCulling(entity: LightningBolt): Boolean = false

    private fun lightningQuad(
        pose: Matrix4fc,
        buffer: VertexConsumer,
        xo0: Float,
        zo0: Float,
        h: Int,
        xo1: Float,
        zo1: Float,
        boltRed: Float,
        boltGreen: Float,
        boltBlue: Float,
        rr1: Float,
        rr2: Float,
        px1: Boolean,
        pz1: Boolean,
        px2: Boolean,
        pz2: Boolean
    ) {
        buffer
            .addVertex(pose, xo0 + (if (px1) rr2 else -rr2), (h * 16).toFloat(), zo0 + (if (pz1) rr2 else -rr2))
            .setColor(boltRed, boltGreen, boltBlue, 0.3f)
        buffer
            .addVertex(pose, xo1 + (if (px1) rr1 else -rr1), ((h + 1) * 16).toFloat(), zo1 + (if (pz1) rr1 else -rr1))
            .setColor(boltRed, boltGreen, boltBlue, 0.3f)
        buffer
            .addVertex(pose, xo1 + (if (px2) rr1 else -rr1), ((h + 1) * 16).toFloat(), zo1 + (if (pz2) rr1 else -rr1))
            .setColor(boltRed, boltGreen, boltBlue, 0.3f)
        buffer
            .addVertex(pose, xo0 + (if (px2) rr2 else -rr2), (h * 16).toFloat(), zo0 + (if (pz2) rr2 else -rr2))
            .setColor(boltRed, boltGreen, boltBlue, 0.3f)
    }
    
    class LightningBoltCypherRenderState : LightningBoltRenderState(), 
        ICypherEntityRenderState by CypherRenderStateDelegate()
}
