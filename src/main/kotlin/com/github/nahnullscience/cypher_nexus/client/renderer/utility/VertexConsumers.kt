package com.github.nahnullscience.cypher_nexus.client.renderer.utility

import com.mojang.blaze3d.vertex.PoseStack.Pose
import com.mojang.blaze3d.vertex.QuadInstance
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormatElement
import net.minecraft.client.resources.model.geometry.BakedQuad
import net.neoforged.neoforge.client.model.quad.BakedNormals
import net.neoforged.neoforge.client.model.quad.MutableQuad
import org.joml.Matrix3f
import org.joml.Matrix3x2fc
import org.joml.Matrix4fc
import org.joml.Vector3f
import org.joml.Vector3fc

class TintedVertexConsumer(
    private val delegate: VertexConsumer,
    private val redFilter: Float,   // 0.0f to 1.0f
    private val greenFilter: Float, // 0.0f to 1.0f
    private val blueFilter: Float,  // 0.0f to 1.0f
    private val alphaFilter: Float = 1.0f // 0.0f to 1.0f
) : VertexConsumer by delegate {

    override fun setColor(r: Int, g: Int, b: Int, a: Int): VertexConsumer {
        val newR = (r * redFilter).toInt().coerceIn(0, 255)
        val newG = (g * greenFilter).toInt().coerceIn(0, 255)
        val newB = (b * blueFilter).toInt().coerceIn(0, 255)
        val newA = (a * alphaFilter).toInt().coerceIn(0, 255)

        return delegate.setColor(newR, newG, newB, newA)
    }
}