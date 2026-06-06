package com.github.nahnullscience.cypher_nexus.client.renderer

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider

abstract class AbstractCypherRenderer <CY : AbstractCypherProjectile> (
    context: EntityRendererProvider.Context
) : EntityRenderer<CY>(context) {
    override fun getTextureLocation(entity: CY) = CypherNexus.modResource("textures/entity/some_texture.png")

    abstract override fun render(
        projectile: CY,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    )
}