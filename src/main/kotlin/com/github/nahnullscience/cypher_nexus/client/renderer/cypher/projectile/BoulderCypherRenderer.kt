package com.github.nahnullscience.cypher_nexus.client.renderer.cypher.projectile

import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.AbstractCypherRenderer
import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.projectile.BoulderCypherRenderer.BoulderCypherRenderState
import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.BlockProjectileRenderState
import com.github.nahnullscience.cypher_nexus.content.entity.projectile.Boulder
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.block.BlockModelRenderState
import net.minecraft.client.renderer.block.model.BlockDisplayContext
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.client.renderer.entity.TntMinecartRenderer
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.util.Mth
import net.minecraft.world.level.block.Blocks

class BoulderCypherRenderer (
    context: Context
) : AbstractCypherRenderer<Boulder, BoulderCypherRenderState>(context) {
    private val blockDisplayContext: BlockDisplayContext = BlockDisplayContext.create()
    private val blockModelResolver = context.blockModelResolver
    private val blockState = Blocks.TNT.defaultBlockState()

    override fun submit(
        state: BoulderCypherRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
        poseStack.pushPose()
        poseStack.translate(0.0f, 0.5f, 0.0f)
        val fuse: Float = state.fuseRemainingInTicks
        if (state.fuseRemainingInTicks < 10.0f) {
            var g: Float = 1.0f - state.fuseRemainingInTicks / 10.0f
            g = Mth.clamp(g, 0.0f, 1.0f)
            g *= g
            g *= g
            val s = 1.0f + g * 0.3f
            poseStack.scale(s, s, s)
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f))
        poseStack.translate(-0.5f, -0.5f, 0.5f)
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0f))
        if (!state.blockState.isEmpty()) {
            TntMinecartRenderer.submitWhiteSolidBlock(
                state.blockState,
                poseStack,
                submitNodeCollector,
                state.lightCoords,
                fuse.toInt() / 5 % 2 == 0,
                state.outlineColor
            )
        }

        poseStack.popPose()
        super.submit(state, poseStack, submitNodeCollector, camera)
    }


    override fun createRenderState() = BoulderCypherRenderState()
    override fun extractRenderState(entity: Boulder, state: BoulderCypherRenderState, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
        this.blockModelResolver.update(state.blockState, blockState, blockDisplayContext)
    }

    class BoulderCypherRenderState : BlockProjectileRenderState() {
        val blockState: BlockModelRenderState = BlockModelRenderState()
        val fuseRemainingInTicks = 3f + partialTick
    }
}
