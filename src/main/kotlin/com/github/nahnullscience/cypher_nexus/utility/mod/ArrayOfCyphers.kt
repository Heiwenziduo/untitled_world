package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.EmptyCypher

/** fixed length, cypher changeable, EmptyCypher autofill */
class ArrayOfCyphers(val capacity: Int = 1) : Iterable<AbstractCypher> {
    init {
        // let's make sure capa > 0
    }
    constructor(list: List<AbstractCypher>) : this(list.size) {
        list.withIndex().forEach { (i, cypher) -> _cyphers[i] = cypher }
    }

    val size = capacity

    // An Array is naturally fixed-length and very fast!
    // We fill it entirely with EmptyCypher upon creation.
    private val _cyphers: Array<AbstractCypher> = Array(capacity) { EmptyCypher }

    // Overriding the bracket operator allows you to do: myInventory[0]
    operator fun get(index: Int): AbstractCypher {
        return _cyphers[index]
    }

    // Overriding allows you to do: myInventory[0] = FireballCypher()
    operator fun set(index: Int, cypher: AbstractCypher?) {
        // If someone tries to set a slot to null, we safely enforce the EmptyCypher rule!
        _cyphers[index] = cypher ?: EmptyCypher
    }
    fun remove(index: Int) = set(index, null)

    /** find the first Empty then replace that with given cypher
     * @return the replaced index, -1 if no empty */
    fun add(cypher: AbstractCypher) : Int {
        // TODO
        return -1
    }

    fun switch(i0: Int, i1: Int) {
        if (i0 >= size || i1 >= size) return
        val t = this[i0]
        this[i0] = this[i1]
        this[i1] = t
    }

    // Allows you to use this class in a for-loop: for (cypher in myInventory)
    override fun iterator(): Iterator<AbstractCypher> {
        return _cyphers.iterator()
    }

    fun clearAll() {
        for (i in _cyphers.indices) {
            _cyphers[i] = EmptyCypher
        }
    }

    fun getActiveCyphers(): List<AbstractCypher> {
        return _cyphers.filter { it !is EmptyCypher }
    }

    fun toList(): List<AbstractCypher> = _cyphers.toList()

    companion object {
        fun of(list: List<AbstractCypher?>) : ArrayOfCyphers {
            val arr = ArrayOfCyphers(list.size)
            list.withIndex().forEach { (i, cypher) -> arr[i] = cypher }
            return arr
        }
    }

    override fun toString(): String {
        return super.toString()
    }
}