package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.utility.mod.AttributeFastOperatorMap.Companion.OperatorMap
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap
import net.minecraft.core.Holder


/**
 * stands as an attribute-holder for a variety of usage.
 * the backing R2D map uses [Double.NaN] as default return value
 * */
class AttributeFastMap(capa: Int = 16) : Reference2DoubleOpenHashMap<CypherAttribute>(capa) {
    constructor(map: Map<CypherAttribute, Double>) : this(map.size) { putAll(map) }
    init {
        defaultReturnValue(DEFAULT_RETURN)
    }

    fun initFromShotState(state: ShotStateChunk, cypher: AbstractProjectileCypher<*>) {
        state.attributes.forEach { (attr, opMap) ->
            if (!attr.isAttributeForCE) return@forEach
            // TODO prune cumulation, some of attributes will not be used, depends on cypher implementation
            this.compute(attr) { key, old -> attributeCalculator(attr, opMap, cypher) }
        }
    }

    fun hasAttribute(attr: CypherAttribute): Boolean = containsKey(attr)

    fun getAttributeOrDefault(attr: CypherAttribute): Double {
        val v = getDouble(attr)
        return if (v.isNaN()) attr.defaultValue else v
    }

    fun setAttribute(attr: CypherAttribute, value: Double): Double = put(attr, value)


    companion object {
        private const val DEFAULT_RETURN = Double.NaN

        fun attributeCalculator(attr: Holder<CypherAttribute>, map: OperatorMap, cypher: AbstractProjectileCypher<*>? = null) =
            attributeCalculator(attr.value(), map, cypher)
        fun attributeCalculator(attr: CypherAttribute, map: OperatorMap, cypher: AbstractProjectileCypher<*>? = null): Double {
            val base = cypher?.getAttrOrDefault(attr) ?: attr.defaultValue
            return AttributeOperator.attributeCalculator(base, map, attr.min, attr.max)
        }
        fun attributeCalculator(attr: CypherAttribute, map: OperatorMap, base: Double): Double {
            return AttributeOperator.attributeCalculator(base, map, attr.min, attr.max)
        }
    }
}
