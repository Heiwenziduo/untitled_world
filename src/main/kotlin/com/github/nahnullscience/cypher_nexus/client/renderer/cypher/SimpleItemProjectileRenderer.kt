package com.github.nahnullscience.cypher_nexus.client.renderer.cypher

import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.ItemProjectileRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.client.renderer.item.ItemModelResolver
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class SimpleItemProjectileRenderer <CE : AbstractDedicatedCypherProjectile> (
    context: Context,
    val item: Item
) : AbstractCypherRenderer<CE, ItemProjectileRenderState>(context) {
    constructor(context: Context) : this(context, Items.AIR)

    companion object {
        const val DEFAULT_SCALE = 0.5f
    }

    private val itemModelResolver: ItemModelResolver = context.itemModelResolver

//    @Volatile // assume renders remain single threaded in production environment
    private var _cachedStack: ItemStack? = null
    val stack: ItemStack
        get() {
            val local = _cachedStack
            return local ?: item.defaultInstance.also { _cachedStack = it }
        }


    init {
        // println("SimpleItemProjectileRenderer init") // called when load into main menu
    }

    override fun submit(
        state: ItemProjectileRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState,
    ) {
        poseStack.pushPose()
        poseStack.scaleByEffectRadius(state)
        poseStack.scale(DEFAULT_SCALE, DEFAULT_SCALE, DEFAULT_SCALE)
        poseStack.mulPose(camera.orientation)
        poseStack.translate(0f, -0.125f, 0f) // put item in the center of a bounding box, no matter how it scaled
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

    override fun createRenderState() = ItemProjectileRenderState()

    override fun extractRenderState(entity: CE, state: ItemProjectileRenderState, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
        itemModelResolver.updateForNonLiving(state.item, stack, ItemDisplayContext.GROUND, entity)
    }
}