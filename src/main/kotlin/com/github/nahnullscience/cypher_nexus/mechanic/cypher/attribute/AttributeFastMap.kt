package com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute

import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap


/**
 * stands as an attribute-holder for a variety of usage.
 * the backing R2D map uses [Double.NaN] as default return value
 * */
class AttributeFastMap(capa: Int = 16) : Reference2DoubleOpenHashMap<CypherAttribute>(capa) {
    constructor(map: Map<CypherAttribute, Double>) : this(map.size) { putAll(map) }
    init {
        defaultReturnValue(DEFAULT_RETURN)
    }

    fun hasAttribute(attr: CypherAttribute): Boolean = containsKey(attr)

    fun getAttributeOrDefault(attr: CypherAttribute): Double {
        val v = getDouble(attr)
        return if (v.isNaN()) attr.defaultValue else v
    }

    fun setAttribute(attr: CypherAttribute, value: Double): Double = put(attr, value)


    companion object {
        private const val DEFAULT_RETURN = Double.NaN
    }
}
