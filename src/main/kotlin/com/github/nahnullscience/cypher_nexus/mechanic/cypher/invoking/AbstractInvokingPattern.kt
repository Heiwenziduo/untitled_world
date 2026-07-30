package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.utility.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.i.IRegisterable
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier

abstract class AbstractInvokingPattern(
    override val resource: Identifier
) : IRegisterable {
    constructor(path: String) : this(CypherNexus.modResource(path))

    abstract fun layout(
        index: Int,
        total: Int,
        coordinate: CoordinateDefinition,
        posDire: PosDirePair
    ): PosDirePair

    private val translationKey = "invoking.${resource.namespace}.pattern.${resource.path}"
    override fun translation(): MutableComponent = Component.translatable(translationKey)
}