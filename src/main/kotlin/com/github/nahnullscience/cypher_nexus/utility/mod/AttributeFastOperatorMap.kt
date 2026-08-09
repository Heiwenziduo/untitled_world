package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.utility.mod.AttributeFastOperatorMap.Companion.OperatorMap
import com.google.common.collect.EnumBiMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import java.util.*
import kotlin.collections.MutableMap.MutableEntry

/**
 *
 * */
class AttributeFastOperatorMap(capa: Int = 16) : Reference2ObjectOpenHashMap<CypherAttribute, OperatorMap>(capa) {
    constructor(map: Map<CypherAttribute, OperatorMap>) : this(map.size) { putAll(map) }
    init {
        defaultReturnValue(null)
    }
    companion object {
        typealias OperatorMap = EnumMap<AttributeOperator, Double>
    }
}
