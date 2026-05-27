package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategoryRegistry
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.EmptyCypher

// TODO maybe let Cypher-s decide
private val AbstractCypher.isInvokable: Boolean
    get() = isNotEmpty() && category != CypherCategoryRegistry.PASSIVE
private val AbstractCypher.isPassive: Boolean
    get() = isNotEmpty() && category == CypherCategoryRegistry.PASSIVE

/** fixed length, cypher changeable, EmptyCypher autofill */
class ArrayOfCyphers(private val capacity: Int = 1) : Iterable<AbstractCypher> {
    /** not empty and not "passive" */
    private var _bitInvokable: Long = 0
    private var _bitPassive: Long = 0
    private val _cyphers: Array<AbstractCypher> = Array(capacity) { EmptyCypher }

    companion object {
        /** O(n) */
        fun of(list: List<AbstractCypher?>) : ArrayOfCyphers = ArrayOfCyphers(list)

        const val MAX_LENGTH = 64 // max length capped at a Long-bits count, guess this is quite enough
    }
    init {
        // let's make sure capa > 0
        require(capacity > 0 && capacity <= MAX_LENGTH)
    }
    /** O(n) */
    constructor(list: List<AbstractCypher?>) : this(list.size) {
        list.withIndex().forEach { (i, cypher) ->
            if (cypher == null) return@forEach
            _cyphers[i] = cypher

            if (cypher.isInvokable) {
                _bitInvokable = _bitInvokable or (1L shl i)
            } else if (cypher.isPassive) {
                _bitPassive = _bitPassive or (1L shl i)
            }
        }
    }

    val size : Int
        get() = capacity

    // this allows index-get: myInventory[0]
    operator fun get(index: Int): AbstractCypher = _cyphers[index]
    // this allows for-loop
    override fun iterator(): Iterator<AbstractCypher> = _cyphers.iterator()
    // this allows index-set: myInventory[0] = FireballCypher()
    operator fun set(index: Int, cypher0: AbstractCypher?) {
        val cypher = cypher0 ?: EmptyCypher
        _cyphers[index] = cypher

        if (cypher.isInvokable) _bitInvokable = _bitInvokable or (1L shl index)
        if (cypher.isPassive) _bitPassive = _bitPassive or (1L shl index)
        if (cypher.isEmpty()) {
            _bitInvokable = _bitInvokable and (1L shl index).inv()
            _bitPassive = _bitPassive and (1L shl index).inv()
        }
    }
    fun remove(index: Int) = set(index, null)
    /** O(n) */
    fun toList(): List<AbstractCypher> = _cyphers.toList()
    /** Empty -> null */
    fun toNullableList(): List<AbstractCypher?> = _cyphers.map { if (it.isEmpty()) null else it }
    fun copy(): ArrayOfCyphers = ArrayOfCyphers(_cyphers.toList())

    /** find the first Empty then replace that with given cypher
     * @return the replaced index, -1 if no empty */
    fun add(cypher: AbstractCypher) : Int {
        // O(1) due to _bit-s
        val first = (_bitInvokable or _bitPassive).inv().countTrailingZeroBits()
        if (first < capacity) {
            set(first, cypher)
            return first
        }
        return -1
    }

    fun switch(i0: Int, i1: Int) {
        if (i0 >= capacity || i1 >= capacity) return
        val t = this[i0]
        this[i0] = this[i1] // this calls #set internally
        this[i1] = t
    }

    fun clearAll() {
        _bitInvokable = 0
        _bitPassive = 0
        for (i in _cyphers.indices) {
            _cyphers[i] = EmptyCypher
        }
    }

    /** gets a bit representation of cyphers, non-invokable set to 0 */
    fun bits(): Long {
        // O(1) due to _bit-s
        return _bitInvokable
    }

    fun firstInvokable(): AbstractCypher? {
        val i = _bitInvokable.countTrailingZeroBits()
        if (i < capacity) {
            return _cyphers[i]
        }
        return null
    }
    fun lastInvokable(): AbstractCypher? {
        val i = 63 - _bitInvokable.countLeadingZeroBits()
        if (i < capacity && i >= 0) {
            return _cyphers[i]
        }
        return null
    }
    fun nextInvokable(index: Int): AbstractCypher? {
        val l = (_bitInvokable shr index).countTrailingZeroBits()
        val t = index + l
        if (t < capacity) {
            return _cyphers[t]
        }
        return null
    }


    override fun toString(): String {
        return _cyphers.toString()
    }
}