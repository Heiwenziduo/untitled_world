package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap

/**
 * ccMap or MoCC,
 * serve as a token to ship across network and rebuild the `ShotStateChunk` on the other side
 * (payload info not included)
 * */
open class MapOfCypherCounts(
    private val r2IMap: Reference2IntOpenHashMap<AbstractCypher> = Reference2IntOpenHashMap(32),
) : MutableMap<AbstractCypher, Int> by r2IMap {
    constructor(anyMap: Map<AbstractCypher, Int>) : this(Reference2IntOpenHashMap(anyMap))

    companion object {
        fun ofSize(s: Int) = MapOfCypherCounts(Reference2IntOpenHashMap(s))
    }

    var max: Int = 0
        private set

    /**
     *
     * */
    fun count(cy: AbstractCypher, n: Int = 1): Int {
        val i = r2IMap.addTo(cy, n)
        if (max < i + n) max = i + n
        return i
    }

    fun getMap() = r2IMap.toMap()
    fun getMutableMap() = r2IMap.toMutableMap()

    override fun toString() = r2IMap.toString()
}