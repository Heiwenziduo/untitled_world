package com.github.nahnullscience.cypher_nexus.client.renderer.cypher

import com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component.EmptyRenderState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.client.renderer.state.level.CameraRenderState

open class InvisibleRenderer <CE : AbstractDedicatedCypherProjectile> (
    context: Context
) : AbstractCypherRenderer<CE, EmptyRenderState>(context) {

//    override fun submit(
//        state: EmptyRenderState,
//        poseStack: PoseStack,
//        submitNodeCollector: SubmitNodeCollector,
//        camera: CameraRenderState
//    ) = Unit
    override fun createRenderState() = EmptyRenderState()
}