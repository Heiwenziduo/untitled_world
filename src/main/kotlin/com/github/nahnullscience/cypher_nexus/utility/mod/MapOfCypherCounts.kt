package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
import kotlin.collections.MutableMap.MutableEntry

/**
 * ccMap or MoCC,
 * serve as a token to ship across network and rebuild the `ShotStateChunk` on the other side
 * (payload info not included)
 *
 * */
open class MapOfCypherCounts(
    private val fastMap: Reference2IntOpenHashMap<AbstractCypher> = Reference2IntOpenHashMap(32),
) : MutableMap<AbstractCypher, Int> {
    constructor(anyMap: Map<AbstractCypher, Int>) : this(Reference2IntOpenHashMap(anyMap))
    init {
        fastMap.defaultReturnValue(DEFAULT_RETURN)
    }

    companion object {
        private const val DEFAULT_RETURN = 0

    }

    var max: Int = 0
        private set

    /**
     *
     * */
    fun count(cy: AbstractCypher, n: Int = 1): Int {
        val i = fastMap.addTo(cy, n) // count from DEFAULT_RETURN
        max = max.coerceAtLeast(i + n)
        return i
    }

    fun clone() = MapOfCypherCounts(fastMap.clone())

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun toString() = fastMap.toString()
    override val size: Int get() = fastMap.size
    override val keys: MutableSet<AbstractCypher> get() = fastMap.keys
    override val values: MutableCollection<Int> get() = fastMap.values
    override val entries: MutableSet<MutableEntry<AbstractCypher, Int>> get() = fastMap.reference2IntEntrySet() as MutableSet<MutableEntry<AbstractCypher, Int>>

    override fun isEmpty(): Boolean  = fastMap.isEmpty()

    override fun containsKey(key: AbstractCypher): Boolean = fastMap.containsKey(key)

    override fun containsValue(value: Int): Boolean = fastMap.containsValue(value)

    override fun get(key: AbstractCypher): Int? {
        val v = fastMap.getInt(key)
        return if (v != DEFAULT_RETURN) v else null
    }

    override fun put(key: AbstractCypher, value: Int): Int? {
        val v = fastMap.put(key, value)
        return if (v != DEFAULT_RETURN) v else null
    }

    override fun remove(key: AbstractCypher): Int? {
        val v = fastMap.removeInt(key)
        return if (v != DEFAULT_RETURN) v else null
    }

    override fun putAll(from: Map<out AbstractCypher, Int>) = fastMap.putAll(from)

    override fun clear() {
        fastMap.clear()
    }
}