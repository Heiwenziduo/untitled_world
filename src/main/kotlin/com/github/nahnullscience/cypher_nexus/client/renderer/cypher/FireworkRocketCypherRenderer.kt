package com.github.nahnullscience.cypher_nexus.client.renderer.cypher

import com.github.nahnullscience.cypher_nexus.client.particle.addCypherTrailParticle
import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.FireworkRocketCypherRenderState
import com.github.nahnullscience.cypher_nexus.content.entity.projectiles.FireworkRocket
import com.github.nahnullscience.cypher_nexus.utility.ANG_2_RAD_F
import com.github.nahnullscience.cypher_nexus.utility.linearInterpolateGaps
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.client.renderer.item.ItemModelResolver
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class FireworkRocketCypherRenderer(
    context: Context
) : AbstractCypherRenderer<FireworkRocket, FireworkRocketCypherRenderState>(context) {
    companion object {
        const val UPWARD_TEXTURE_Z_ROT_RAD = -90f * ANG_2_RAD_F
    }
    private val itemModelResolver: ItemModelResolver = context.itemModelResolver

    private var _stackBacking: ItemStack? = null
    private val stack get() = _stackBacking ?: Items.FIREWORK_ROCKET.defaultInstance.also { _stackBacking = it }

    override fun submit(
        state: FireworkRocketCypherRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
        poseStack.pushPose()
//        poseStack.scaleByEffectRadius(state)
        poseStack.scale(0.75f, 0.75f, 0.75f)
        poseStack.rotateToSpeed(state) {
            rotateZ(UPWARD_TEXTURE_Z_ROT_RAD)
            rotateY(state.selfRotate * ANG_2_RAD_F)
        }
        poseStack.translate(0f, -0.125f, 0f)
        state.item.submit(
            poseStack,
            submitNodeCollector,
            state.lightCoords,
            OverlayTexture.NO_OVERLAY,
            state.outlineColor
        )
        poseStack.popPose()

        super.submit(state, poseStack, submitNodeCollector, camera)
    }

    override fun addTrailParticles(
        level: ClientLevel,
        entity: FireworkRocket,
        x: Double,
        y: Double,
        z: Double,
        xo: Double,
        yo: Double,
        zo: Double
    ) {
        val speed = entity.knownMovement
        val random = entity.random
        linearInterpolateGaps(xo, yo, zo, x, y, z, 0.4) { step, x, y, z ->
            addCypherTrailParticle(
                entity,
                ParticleTypes.FIREWORK,
                x, y, z,
                -speed.x * random.nextGaussian() * 0.33,
                -speed.y * random.nextDouble() * 0.33,
                -speed.z * random.nextGaussian() * 0.33
            ) {
                lifetime = 11
            }
        }
    }

    override fun createRenderState() = FireworkRocketCypherRenderState()
    override fun extractRenderState(entity: FireworkRocket, state: FireworkRocketCypherRenderState, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
        itemModelResolver.updateForNonLiving(state.item, stack, ItemDisplayContext.GROUND, entity)
        state.selfRotate = entity.selfRotate.toFloat() + partialTicks
    }
}
