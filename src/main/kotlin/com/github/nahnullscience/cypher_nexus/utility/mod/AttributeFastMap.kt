package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap


/**
 * stands as an attribute-holder for a variety of usage
 * */
@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
class AttributeFastMap(
    private val fastMap: Reference2DoubleArrayMap<CypherAttribute> = Reference2DoubleArrayMap()
) : Reference2DoubleMap<CypherAttribute> by fastMap {
    constructor(map: Map<CypherAttribute, Double>) : this(Reference2DoubleArrayMap(map))
    init {
        fastMap.defaultReturnValue(DEFAULT_RETURN)
    }

    companion object {
        private const val DEFAULT_RETURN = -Double.MAX_VALUE

//        fun <A : AbstractReference2DoubleFunction<*>> A.setDefault(default: Double) : A =
//            apply { defaultReturnValue(default) }
    }

    fun getAttrOrDefault(attr: CypherAttribute): Double {
        val v = getDouble(attr)
        return if (v == DEFAULT_RETURN) attr.defaultValue else v
    }

    fun getAttrOrNull(attr: CypherAttribute): Double? {
        val v = getDouble(attr)
        return if (v == DEFAULT_RETURN) attr.defaultValue else null
    }

    fun initFromShotState(state: ShotStateChunk, cypher: AbstractProjectileCypher<*>) {
        state.attr2opMap.forEach { (attr, opMap) ->
            if (!attr.isCEAttribute) return@forEach
//            if (haveFlag(CypherFlags.CONSTANT_EXISTING) && CypherAttributes.EXISTING.`is`(attr.resource)) return@forEach
            // TODO prune cumulation, some of attributes will not be used, depends on cypher implementation

            this.compute(attr) { a, v ->
                val base = cypher.getAttrBaseOrDefault(attr)
                val final = AttributeOperator.attributeCalculator(base, opMap)
                attr.restrictRange(final)
            }
        }
    }


    override fun put(key: CypherAttribute?, value: Double): Double {
        return fastMap.put(key, value)
    }

    override fun defaultReturnValue(p0: Double) {
        fastMap.defaultReturnValue(p0)
    }

    override fun defaultReturnValue(): Double {
        return fastMap.defaultReturnValue()
    }

    override fun clear() {
        fastMap.clear()
    }

//    override fun getOrDefault(key: Any?, defaultValue: Double): Double {
//        return r2DMap.getOrDefault(key, defaultValue)
//    }

    @Deprecated("Deprecated in Java")
    override fun getOrDefault(key: Any?, defaultValue: Double?): Double? {
        return fastMap.getOrDefault(key, defaultValue)
    }

    override fun remove(key: Any?, value: Double): Boolean {
        return fastMap.remove(key, value)
    }

    override fun removeDouble(key: Any?): Double {
        return fastMap.removeDouble(key)
    }

    override fun containsValue(p0: Double): Boolean {
        return fastMap.containsValue(p0)
    }
}