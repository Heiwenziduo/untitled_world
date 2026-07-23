package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import it.unimi.dsi.fastutil.objects.Reference2IntMap
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap

/**
 * ccMap or MoCC,
 * serve as a token to ship across network and rebuild the `ShotStateChunk` on the other side
 * (payload info not included)
 * */
@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
open class MapOfCypherCounts(
    private val fastMap: Reference2IntOpenHashMap<AbstractCypher> = Reference2IntOpenHashMap(32),
) : Reference2IntMap<AbstractCypher> by fastMap {
    constructor(anyMap: Map<AbstractCypher, Int>) : this(Reference2IntOpenHashMap(anyMap))

    companion object {
        fun ofSize(s: Int) = MapOfCypherCounts(Reference2IntOpenHashMap(s))
    }

    var max: Int = -1
        private set

    /**
     *
     * */
    fun count(cy: AbstractCypher, n: Int = 1): Int {
        val i = fastMap.addTo(cy, n)
        if (max < i + n) max = i + n
        return i
    }

    fun getMap() = fastMap.toMap()
    fun getMutableMap() = fastMap.toMutableMap()

    override fun toString() = fastMap.toString()

    override fun put(key: AbstractCypher?, value: Int): Int {
        return fastMap.put(key, value)
    }

    override fun defaultReturnValue(p0: Int) {
        fastMap.defaultReturnValue(p0)
    }

    override fun defaultReturnValue(): Int {
        return fastMap.defaultReturnValue()
    }

    override fun clear() {
        fastMap.clear()
    }

//    override fun getOrDefault(key: Any?, defaultValue: Int): Int {
//        return r2IMap.getOrDefault(key, defaultValue)
//    }

    @Deprecated("Deprecated in Java")
    override fun getOrDefault(key: Any?, defaultValue: Int?): Int? {
        return fastMap.getOrDefault(key, defaultValue)
    }

    override fun remove(key: Any?, value: Int): Boolean {
        return fastMap.remove(key, value)
    }

    override fun removeInt(key: Any?): Int {
        return fastMap.removeInt(key)
    }

    override fun containsValue(p0: Int): Boolean {
        return fastMap.containsValue(p0)
    }
}