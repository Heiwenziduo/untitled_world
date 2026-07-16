package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher

/**
 * serve as a token to rebuild the ProjectileStateChunk (payload info not included)
 * */
//open class MapOfCypherCounts : Reference2IntOpenHashMap<AbstractCypher>() {
// TODO data type polishing required
open class MapOfCypherCounts(private val map: HashMap<AbstractCypher, Int>) : MutableMap<AbstractCypher, Int> by map {
    constructor(anyMap: Map<AbstractCypher, Int>) : this(HashMap<AbstractCypher, Int>(anyMap))

    companion object {
        fun of() = MapOfCypherCounts(HashMap())
    }

    override fun get(key: AbstractCypher): Int {
        return map[key] ?: 0
    }

    fun innerMap() = map

    override fun toString() = map.toString()

    /**
     *
     * */
    fun count(cy: AbstractCypher, n: Int = 1): Int {
        val v = map.getOrPut(cy) { 0 }
        map[cy] = v + n
        return v
    }
}