package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.utility.mod.AttributeFastOpMap.Companion.OperatorMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import java.util.*

/**
 *
 * */
class AttributeFastOpMap(
    private val fastMap: Reference2ObjectOpenHashMap<CypherAttribute, OperatorMap> = Reference2ObjectOpenHashMap(8)
) : Reference2ObjectMap<CypherAttribute, OperatorMap> by fastMap {
    constructor(map: Map<CypherAttribute, OperatorMap>) : this(Reference2ObjectOpenHashMap(map))
    init {
        defaultReturnValue(null)
    }

    companion object {
        typealias OperatorMap = EnumMap<AttributeOperator, Double>
    }

    override fun put(key: CypherAttribute?, value: OperatorMap): OperatorMap? {
        return fastMap.put(key, value)
    }

    override fun remove(key: CypherAttribute?): OperatorMap? {
        return fastMap.remove(key)
    }

    override fun defaultReturnValue(p0: OperatorMap?) {
        fastMap.defaultReturnValue(p0)
    }

    override fun defaultReturnValue(): OperatorMap? {
        return fastMap.defaultReturnValue()
    }

    override fun clear() {
        fastMap.clear()
    }
}