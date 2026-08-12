package com.github.nahnullscience.cypher_nexus.client.renderer.cypher

import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component.ICypherEntityRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.utility.ang2Rad
import com.github.nahnullscience.cypher_nexus.utility.toVec3
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.entity.Entity
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

abstract class AbstractCypherRenderer <CE, State> (
    context: Context
) : EntityRenderer<CE, State>(context)
        where CE : Entity, CE : ICypherEntity,
              State : EntityRenderState, State : ICypherEntityRenderState
{

//    override fun submit(
//        state: State,
//        poseStack: PoseStack,
//        submitNodeCollector: SubmitNodeCollector,
//        camera: CameraRenderState
//    ) {
//        poseStack.pushPose()
//        poseStack.scale(state.effectRadius, state.effectRadius, state.effectRadius)
//        poseStack.submitCypher(state, submitNodeCollector, camera)
//        poseStack.popPose()
//        super.submit(state, poseStack, submitNodeCollector, camera)
//    }
//
//
//    protected open fun submitCypher(
//        state: State,
//        poseStack: PoseStack,
//        submitNodeCollector: SubmitNodeCollector,
//        camera: CameraRenderState
//    ) { }
//    protected fun PoseStack.submitCypher(state: State, submitNodeCollector: SubmitNodeCollector, camera: CameraRenderState) =
//        submitCypher(state, this, submitNodeCollector, camera)

    override fun shouldRender(entity: CE, culler: Frustum, camX: Double, camY: Double, camZ: Double): Boolean {
        // FIXME Arrow with large radius culled un-expectedly
        return super.shouldRender(entity, culler, camX, camY, camZ)
    }

    override fun extractRenderState(entity: CE, state: State, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
        state.extractFrom(entity, state)
    }

    /**
     * good timing to add trail particle to the projectile, called after tick-logic has applied
     * */
    open fun addTrailParticles(level: ClientLevel, ce: CE, x: Double, y: Double, z: Double, xo: Double, yo: Double, zo: Double) = Unit

    /**
     *
     * */
    protected fun PoseStack.scaleByEffectRadius(state: State, factor: Float = Float.NaN) {
        val f = if (factor.isNaN()) state.effectRadius else factor * state.effectRadius
        scale(f, f, f)

//        submitNodeCollector.submitParticleGroup() {  }

//        val matrix = last().pose()
//
//        // --- PART B: THE LOCAL PARTICLE BUNDLE ---
//        val coreConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(CORE_TEXTURE))
//
//        // Simulate a cluster of orbiting "fake" particles around the core using math expressions
//        val count = 3
//        for (i in 0 until count) {
//            // Add an offset angle per particle so they form a beautiful constellation shape
//            val phaseOffset = i * (2.0 * Math.PI / count)
//            val time = state.ageInTicks + phaseOffset
//
//            // Calculate a localized orbiting path relative to the moving center (0,0,0)
//            val radius = 0.2
//            val orbitX = sin(time * 0.4) * radius
//            val orbitY = cos(time * 0.4) * radius
//            val orbitZ = sin(time * 0.2) * radius
//
//            // Draw a tiny billboarded particle center quad (0.05 half-size)
//            drawParticleQuad(coreConsumer, matrix, orbitX, orbitY, orbitZ, 0.05f, packedLight)
//        }
    }

    protected fun PoseStack.addTrailEffect(state: State, submitNodeCollector: SubmitNodeCollector, camera: CameraRenderState) {
        // Invert the velocity vector so the trail stretches backwards from local origin (0,0,0)
        val velocity = Vector3f(state.vx.toFloat(), state.vy.toFloat(), state.vz.toFloat())
        val trailX = -velocity.x
        val trailY = -velocity.y
        val trailZ = -velocity.z
        // Generate a simple perpendicular vector for the ribbon's thickness half-width (0.15 blocks)
        val upVec = Vector3f(0f, 1f, 0f)
        val side = velocity.cross(upVec).normalize().mul(0.15f).toVec3()

        // Grab a translucent blending consumer from the multi-buffer pipeline
        submitNodeCollector.submitCustomGeometry(this, RenderTypes.lightning()) { pose, buffer ->
            val matrix = pose.pose()
            // Draw an unbroken quad trail scaling perfectly with the projectile's velocity
            // Vertex 1: Front-Left (Local Origin Offset)
            addTrailVertex(buffer, matrix, side.x, side.y, side.z, 0f, 0f, 255, state.lightCoords)
            // Vertex 2: Front-Right (Local Origin Offset)
            addTrailVertex(buffer, matrix, -side.x, -side.y, -side.z, 1f, 0f, 255, state.lightCoords)
            // Vertex 3: Back-Right (Stretched along vector direction)
            addTrailVertex(buffer, matrix, trailX - side.x, trailY - side.y, trailZ - side.z, 1f, 1f, 0, state.lightCoords) // Alpha 0 = Fade out!
            // Vertex 4: Back-Left (Stretched along vector direction)
            addTrailVertex(buffer, matrix, trailX + side.x, trailY + side.y, trailZ + side.z, 0f, 1f, 0, state.lightCoords)  // Alpha 0 = Fade out!
        }
    }

    // Direct helper method to pack vertex arrays neatly into the buffer stream
    protected fun addTrailVertex(consumer: VertexConsumer, matrix: Matrix4f, x: Double, y: Double, z: Double, u: Float, v: Float, alpha: Int, light: Int) {
        consumer.addVertex(matrix, x.toFloat(), y.toFloat(), z.toFloat())
            .setColor(255, 255, 255, alpha)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(0f, 1f, 0f)
    }


    companion object {
        fun VertexConsumer.drawParticleQuad(matrix: Matrix4f, cx: Double, cy: Double, cz: Double, size: Float, light: Int) {
            // Flat placeholder camera alignment layout around a local offset coordinate point (cx, cy, cz)
            addVertex(matrix, (cx - size).toFloat(), (cy - size).toFloat(), cz.toFloat()).setColor(255, 255, 255, 200).setUv(0f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f)
            addVertex(matrix, (cx + size).toFloat(), (cy - size).toFloat(), cz.toFloat()).setColor(255, 255, 255, 200).setUv(1f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f)
            addVertex(matrix, (cx + size).toFloat(), (cy + size).toFloat(), cz.toFloat()).setColor(255, 255, 255, 200).setUv(1f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f)
            addVertex(matrix, (cx - size).toFloat(), (cy + size).toFloat(), cz.toFloat()).setColor(255, 255, 255, 200).setUv(0f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f)
        }

        inline fun rotateOfSpeed(state: ICypherEntityRenderState, config: Quaternionf.() -> Unit): Quaternionf {
            val yr = (state.yRot - 90.0f).ang2Rad()
            val xr = state.xRot.ang2Rad()
            return Quaternionf().rotateY(yr).rotateZ(xr).also { it.config() }
        }
        fun rotateOfSpeed(state: ICypherEntityRenderState) = rotateOfSpeed(state) { }

        inline fun PoseStack.rotateToSpeed(state: ICypherEntityRenderState, crossinline config: Quaternionf.() -> Unit) {
            mulPose(rotateOfSpeed(state, config))
        }
        fun PoseStack.rotateToSpeed(state: ICypherEntityRenderState) = mulPose(rotateOfSpeed(state))
    }
}