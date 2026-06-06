package com.github.nahnullscience.cypher_nexus.client.cypher.visualizer

import com.github.nahnullscience.cypher_nexus.client.cypher.ICypherVisualizer
import com.github.nahnullscience.cypher_nexus.content.cypher.projectile.EnderTeleportationCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.block.BlockRenderDispatcher
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object EnderTeleportationVi : ICypherVisualizer.IBasicItemVisualizer {
    override fun cypher(): AbstractCypher = EnderTeleportationCypher
    override val stack = ItemStack(Items.ENDER_PEARL)

    override fun render(
        projectile: AbstractCypherProjectile,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        itemRenderer: ItemRenderer,
        blockRenderer: BlockRenderDispatcher,
        entityRenderDispatcher: EntityRenderDispatcher
    ) {
        super.render(projectile, entityYaw, partialTick, poseStack, bufferSource, packedLight, itemRenderer, blockRenderer, entityRenderDispatcher)
        for (i in 0..4) {
            projectile.level().addParticle(
                // ParticleUtils
                ParticleTypes.DRAGON_BREATH,
                projectile.x + projectile.deltaMovement.x * partialTick,
                projectile.y + projectile.deltaMovement.y * partialTick,
                projectile.z + projectile.deltaMovement.z * partialTick,

//                -projectile.deltaMovement.x,
//                -projectile.deltaMovement.y,
//                -projectile.deltaMovement.z,
                0.0, 0.0, 0.0
            )
        }

    }

}