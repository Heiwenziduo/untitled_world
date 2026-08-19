package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.utility.i.IRegisterable
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate.Companion.vectorsConsumer2
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier

abstract class AbstractInvokingPattern(
    override val resource: Identifier
) : IRegisterable {
    constructor(path: String) : this(CypherNexus.modResource(path))

    /**
     * store result position & direction to coordinate-cache in the form of raw double.
     *
     * note [index] starts from 0, and [total] counts from 1.
     * @return the starting number to extract the cache, the true index is 6 * the value
     * */
    abstract fun arrangeVectors(
        index: Int,
        total: Int,
        coordinate: AnchoredCoordinate
    ): Int

    inline fun layout(
        index: Int,
        total: Int,
        coordinate: AnchoredCoordinate,
        crossinline then: vectorsConsumer2
    ) {
        val target = arrangeVectors(index, total, coordinate)
        coordinate.extractCache(target, then)
    }

    private val translationKey = "invoking.${resource.namespace}.pattern.${resource.path}"
    override fun translation(): MutableComponent = Component.translatable(translationKey)
}