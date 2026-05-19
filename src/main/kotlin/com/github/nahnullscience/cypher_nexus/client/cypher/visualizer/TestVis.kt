package com.github.nahnullscience.cypher_nexus.client.cypher.visualizer

import com.github.nahnullscience.cypher_nexus.client.cypher.ICypherVisualizer
import com.github.nahnullscience.cypher_nexus.content.entity.CypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher._TestModifier
import com.github.nahnullscience.cypher_nexus.mechanic.cypher._TestProjectile
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.block.BlockRenderDispatcher
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.neoforged.neoforge.client.model.data.ModelData

object TestVis {
    object Modifier : ICypherVisualizer {
        override fun cypher() = _TestModifier
        override fun render(
            projectile: CypherProjectile,
            entityYaw: Float,
            partialTick: Float,
            poseStack: PoseStack,
            bufferSource: MultiBufferSource,
            packedLight: Int,
            itemRenderer: ItemRenderer,
            blockRenderer: BlockRenderDispatcher,
            entityRenderDispatcher: EntityRenderDispatcher
        ) {
            projectile.level().addParticle(
                // ParticleUtils
                ParticleTypes.BUBBLE,
//            projectile.x + projectile.deltaMovement.x * partialTick,
//            projectile.y + projectile.deltaMovement.y * partialTick,
//            projectile.z + projectile.deltaMovement.z * partialTick,
                projectile.x,
                projectile.y,
                projectile.z,
                0.0, 0.0, 0.0
            )
        }

    }

    object Projectile : ICypherVisualizer {
        override fun cypher() = _TestProjectile
        val blockState: Block = Blocks.STONE
        override fun render(
            projectile: CypherProjectile,
            entityYaw: Float,
            partialTick: Float,
            poseStack: PoseStack,
            bufferSource: MultiBufferSource,
            packedLight: Int,
            itemRenderer: ItemRenderer,
            blockRenderer: BlockRenderDispatcher,
            entityRenderDispatcher: EntityRenderDispatcher
        ) {
            blockRenderer.renderSingleBlock(
                blockState.defaultBlockState(),
                poseStack,
                bufferSource,
                packedLight,
                0,
                ModelData.EMPTY,
                RenderType.SOLID
            )
        }

    }
}