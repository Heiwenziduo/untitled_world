package com.github.nahnullscience.cypher_nexus.client.renderer.cypher

import com.github.nahnullscience.cypher_nexus.client.particle.addCypherTrailParticle
import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.ItemProjectileRenderState
import com.github.nahnullscience.cypher_nexus.content.entity.projectiles.FireworkRocket
import com.github.nahnullscience.cypher_nexus.utility.linearInterpolateGaps
import com.mojang.blaze3d.vertex.PoseStack
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
) : AbstractCypherRenderer<FireworkRocket, ItemProjectileRenderState>(context) {
    private val itemModelResolver: ItemModelResolver = context.itemModelResolver

    private var _stackBacking: ItemStack? = null
    private val stack get() = _stackBacking ?: Items.FIREWORK_ROCKET.defaultInstance.also { _stackBacking = it }

    override fun submit(
        state: ItemProjectileRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
        poseStack.pushPose()
        poseStack.cypherSetup(state, submitNodeCollector, camera)
        poseStack.scale(0.5f, 0.5f, 0.5f)
        poseStack.mulPose(camera.orientation)
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
                -speed.x * random.nextGaussian() * 0.25,
                -speed.y * random.nextDouble() * 0.25,
                -speed.z * random.nextGaussian() * 0.25
            ) {
                lifetime = 10
            }
        }
    }

    override fun createRenderState() = ItemProjectileRenderState()
    override fun extractRenderState(entity: FireworkRocket, state: ItemProjectileRenderState, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
        itemModelResolver.updateForNonLiving(state.item, stack, ItemDisplayContext.GROUND, entity)
    }
}