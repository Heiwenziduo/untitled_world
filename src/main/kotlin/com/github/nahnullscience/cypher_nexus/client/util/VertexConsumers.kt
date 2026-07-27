package com.github.nahnullscience.cypher_nexus.client.util

import com.mojang.blaze3d.vertex.VertexConsumer

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