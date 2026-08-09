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
    }
}

//class AttributeFastMap(
//    private val fastMap: Reference2DoubleOpenHashMap<CypherAttribute> = Reference2DoubleOpenHashMap()
//) : MutableMap<CypherAttribute, Double> {
//    constructor(map: Map<CypherAttribute, Double>) : this(Reference2DoubleOpenHashMap(map))
//    init {
//        fastMap.defaultReturnValue(DEFAULT_RETURN)
//    }
//
//    fun getAttrOrDefault(attr: CypherAttribute): Double {
//        val v = fastMap.getDouble(attr)
//        return if (v != DEFAULT_RETURN) v else attr.defaultValue
//    }
//
//    fun initFromShotState(state: ShotStateChunk, cypher: AbstractProjectileCypher<*>) {
//        state.attributes.forEach { (attr, opMap) ->
//            if (!attr.isAttributeForCE) return@forEach
//            this.compute(attr) { key, old -> attributeCalculator(attr, opMap, cypher) }
//        }
//    }
//
//    fun clone() = AttributeFastMap(fastMap.clone())
//
//    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//    override fun toString() = fastMap.toString()
//    override val size: Int get() = fastMap.size
//    override val keys: MutableSet<CypherAttribute> get() = fastMap.keys
//    override val values: MutableCollection<Double> get() = fastMap.values
//    override val entries: MutableSet<MutableEntry<CypherAttribute, Double>> get() =
//        fastMap.reference2DoubleEntrySet() as MutableSet<MutableEntry<CypherAttribute, Double>>
//
//    override fun isEmpty(): Boolean  = fastMap.isEmpty()
//
//    override fun containsKey(key: CypherAttribute): Boolean = fastMap.containsKey(key)
//
//    override fun containsValue(value: Double): Boolean = fastMap.containsValue(value)
//
//    @Deprecated("")
//    override fun get(key: CypherAttribute): Double? {
//        val v = fastMap.getDouble(key)
//        return if (v != DEFAULT_RETURN) v else null
//    }
//
//    @Deprecated("")
//    override fun put(key: CypherAttribute, value: Double): Double? {
//        val v = fastMap.put(key, value)
//        return if (v != DEFAULT_RETURN) v else null
//    }
//
//    @Deprecated("")
//    override fun remove(key: CypherAttribute): Double? {
//        val v = fastMap.removeDouble(key)
//        return if (v != DEFAULT_RETURN) v else null
//    }
//
//    override fun putAll(from: Map<out CypherAttribute, Double>) = fastMap.putAll(from)
//
//    override fun clear() {
//        fastMap.clear()
//    }
//}