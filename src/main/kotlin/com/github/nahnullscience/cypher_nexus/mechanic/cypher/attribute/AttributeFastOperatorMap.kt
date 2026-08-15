package com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.utility.finiteOrDefault
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap

/**
 *
 * */
class AttributeFastOperatorMap(capa: Int = 16) : Reference2ObjectOpenHashMap<CypherAttribute, DoubleArray>(capa) {
    constructor(map: Map<CypherAttribute, DoubleArray>) : this(map.size) { putAll(map) }
    init {
        defaultReturnValue(null)
    }

    /***/
    fun getAttributeOrNaN(attr: CypherAttribute, operator: AttributeOperator): Double {
        val da = this[attr] ?: return Double.NaN
        return da[operator.ordinal]
    }

    /***/
    fun setAttribute(attr: CypherAttribute, operator: AttributeOperator, value: Double) {
        val da = this[attr] ?: initOperatorMap().also { this[attr] = it }
        da[operator.ordinal] = value
    }

    /**
     * cumulate attribute value, return the cumulation result
     * */
    fun cumulateAttribute(attr: CypherAttribute, operator: AttributeOperator, value: Double): Double {
        val da = this[attr] ?: initOperatorMap().also { this[attr] = it }
        val old = da.valueOrOperatorDefault(operator)
        return operator.cumulate(old, value).also {
            da[operator.ordinal] = it
        }
    }

    /**
     * cumulate all attributes from another [AttributeFastOperatorMap]
     * */
    inline fun absorb(other: AttributeFastOperatorMap, count: Int = 1, abort: (attr: CypherAttribute) -> Boolean) {
        val operators = AttributeOperator.entries
        other.forEach { (attr, daOther) ->
            if (abort(attr)) return@forEach
            val daThis = this[attr] ?: initOperatorMap().also { this[attr] = it }

            // if set, skip
            val s = daThis[AttributeOperator.SET_ALL.ordinal]
            val sO = daOther[AttributeOperator.SET_ALL.ordinal]
            if (s.isFinite() && sO.isNaN()) return@forEach
            if (sO.isFinite()) {
                daThis[AttributeOperator.SET_ALL.ordinal] = sO
                return@forEach
            }

            for (i in daThis.indices) {
                val new = daOther[i].takeIf { it.isFinite() } ?: continue
                val op = operators[i]
                val old = daThis.valueOrOperatorDefault(op)
                daThis[i] = op.cumulate(old, new, count)
            }
        }
    }

    companion object {
        private const val DEFAULT_INIT = Double.NaN

        @PublishedApi
        internal fun AttributeFastOperatorMap.initOperatorMap(): DoubleArray {
            val entries = AttributeOperator.entries
            return DoubleArray(entries.size) { DEFAULT_INIT }
            // return DoubleArray(entries.size) { i -> entries[i].defaultValue }
        }

        @PublishedApi
        internal fun DoubleArray.valueOrOperatorDefault(op: AttributeOperator): Double {
            return this[op.ordinal].finiteOrDefault { op.defaultValue }
        }

        fun AttributeFastOperatorMap.attrCalculator(
            attr: CypherAttribute,
            cypher: AbstractProjectileCypher<*>? = null
        ) = attrCalculator(attr, cypher?.getAttrOrDefault(attr) ?: attr.defaultValue, attr.min, attr.max)

        fun AttributeFastOperatorMap.attrCalculator(
            attr: CypherAttribute,
            base: Double
        ): Double = attrCalculator(attr, base, attr.min, attr.max)

        fun AttributeFastOperatorMap.attrCalculator(
            attr: CypherAttribute,
            base: Double,
            min: Double,
            max: Double
        ): Double {
            val da = this[attr] ?: return base

            val s = da[AttributeOperator.SET_ALL.ordinal]
            if (s.isFinite()) return s

            val a = da.valueOrOperatorDefault(AttributeOperator.ADD)
            val mB = da.valueOrOperatorDefault(AttributeOperator.MULTIPLY_BASE)
            val mT = da.valueOrOperatorDefault(AttributeOperator.MULTIPLY_TOTAL)
            val cap = da.valueOrOperatorDefault(AttributeOperator.CAP_AT)
            return ((base + a) * (mB + 1) * mT).coerceAtMost(cap).coerceIn(min,  max)
        }
    }
}
