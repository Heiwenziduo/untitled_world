package com.github.nahnullscience.cypher_nexus.client.renderer.cypher.projectile

import com.github.nahnullscience.cypher_nexus.client.particle.addCypherTrailParticle
import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.AbstractCypherRenderer
import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.projectile.FireworkRocketCypherRenderState
import com.github.nahnullscience.cypher_nexus.content.entity.projectile.FireworkRocket
import com.github.nahnullscience.cypher_nexus.content.entity.projectile.FireworkRocket.RandomFireRocket
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getEffectRadius
import com.github.nahnullscience.cypher_nexus.utility.Colors
import com.github.nahnullscience.cypher_nexus.utility.ang2Rad
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
import kotlin.math.PI

class FireworkRocketCypherRenderer(
    context: Context
) : AbstractCypherRenderer<FireworkRocket, FireworkRocketCypherRenderState>(context) {
    companion object {
        const val UPWARD_TEXTURE_Z_ROT_RAD = -(PI / 2).toFloat()
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
            rotateY(state.selfRotate.ang2Rad())
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
        ce: FireworkRocket,
        x: Double,
        y: Double,
        z: Double,
        xo: Double,
        yo: Double,
        zo: Double
    ) {
        val speed = ce.knownMovement
        val random = ce.random
        val scale = ce.getEffectRadius().coerceIn(0.25f, 2f) // vanilla firework can't be easily scale, that's a shame
        linearInterpolateGaps(xo, yo, zo, x, y, z, 0.4) { step, x, y, z ->
            addCypherTrailParticle(
                ParticleTypes.FIREWORK,
                x, y, z,
                -speed.x * random.nextGaussian() * 0.33,
                -speed.y * random.nextDouble() * 0.33,
                -speed.z * random.nextGaussian() * 0.33,
            ) {
                scale(scale)
                lifetime = 11

                if (ce is RandomFireRocket) {
                    if (random.nextDouble() > 0.33) {
                        val id = random.nextInt(15)
                        Colors.vanillaDyeColorsFirework[id].let {
                            setColor(it[0], it[1], it[2])
                        }
                    }
                    if (ce.dyed) {
                        setAlpha(ce.hueFloatArray[3])
                    }
                } else if (ce.dyed) {
                    ce.hueFloatArray.let {
                        setColor(it[0], it[1], it[2])
                        setAlpha(it[3])
                    }
                }
            }
        }
    }

    override fun createRenderState() = FireworkRocketCypherRenderState()
    override fun extractRenderState(entity: FireworkRocket, state: FireworkRocketCypherRenderState, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
        itemModelResolver.updateForNonLiving(state.item, stack, ItemDisplayContext.GROUND, entity)
        state.selfRotate = entity.selfRotate.toFloat() + partialTicks * 3f
    }
}
