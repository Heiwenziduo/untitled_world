package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
import net.minecraft.core.Holder
import kotlin.collections.MutableMap.MutableEntry

/**
 * ccMap or MoCC,
 * serve as a token to ship across network and rebuild the `ShotStateChunk` on the other side
 * (this only includes shot-state attributes that shares among all CE of that state,
 * CE themselves and payload info are not included)
 * */
open class MapOfCypherCounts(capa: Int = 32) : Reference2IntLinkedOpenHashMap<AbstractCypher>(capa) {
    constructor(map: Map<AbstractCypher, Int>) : this(map.size) { putAll(map) }
    init {
        defaultReturnValue(0)
    }

    var max: Int = 0
        private set

    /**
     *
     * */
    fun count(cy: AbstractCypher, n: Int = 1): Int = addTo(cy, n)

    override fun addTo(k: AbstractCypher, incr: Int): Int {
        return super.addTo(k, incr).also { max = max.coerceAtLeast(it + incr) }
    }

    fun getCount(cy: AbstractCypher): Int = getInt(cy)
    fun getCount(cy: Holder<out AbstractCypher>): Int = getInt(cy.value())
}
