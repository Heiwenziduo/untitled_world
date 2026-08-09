package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.utility.mod.AttributeFastOperatorMap.Companion.OperatorMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import java.util.*
import kotlin.collections.MutableMap.MutableEntry

/**
 *
 * */
class AttributeFastOperatorMap(
    private val fastMap: Reference2ObjectOpenHashMap<CypherAttribute, OperatorMap> = Reference2ObjectOpenHashMap(8),
) : MutableMap<CypherAttribute, OperatorMap> {
    constructor(map: Map<CypherAttribute, OperatorMap>) : this(Reference2ObjectOpenHashMap(map))
    init {
        fastMap.defaultReturnValue(null)
    }
    companion object {
        typealias OperatorMap = EnumMap<AttributeOperator, Double>
    }

    fun clone() = AttributeFastOperatorMap(fastMap.clone())

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun toString() = fastMap.toString()
    override val size: Int get() = fastMap.size
    override val keys: MutableSet<CypherAttribute> get() = fastMap.keys
    override val values: MutableCollection<OperatorMap> get() = fastMap.values
    override val entries: MutableSet<MutableEntry<CypherAttribute, OperatorMap>> get() = fastMap.reference2ObjectEntrySet() as MutableSet<MutableEntry<CypherAttribute, OperatorMap>>

    override fun isEmpty(): Boolean  = fastMap.isEmpty()

    override fun containsKey(key: CypherAttribute): Boolean = fastMap.containsKey(key)

    override fun containsValue(value: OperatorMap): Boolean = fastMap.containsValue(value)

    override fun get(key: CypherAttribute): OperatorMap? = fastMap[key]

    override fun put(
        key: CypherAttribute,
        value: OperatorMap
    ): OperatorMap? {
        return fastMap.put(key, value)
    }

    override fun remove(key: CypherAttribute): OperatorMap? {
        return fastMap.remove(key)
    }

    override fun putAll(from: Map<out CypherAttribute, OperatorMap>) = fastMap.putAll(from)

    override fun clear() {
        fastMap.clear()
    }
}