package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.EmptyCypher


private fun AbstractCypher.isPassive(): Boolean = isNotEmpty() && !isInvokable()

/** fixed length, cypher changeable, EmptyCypher autofill */
open class ArrayOfCyphers(val capacity: Int) : Iterable<AbstractCypher> {
    class MutableAoC(capacity: Int) : ArrayOfCyphers(capacity) {
        // TODO consider move #set function here
        fun clearAll() {
            bitsInvokable = 0
            bitsPassive = 0
            for (i in cyphers.indices) {
                cyphers[i] = EmptyCypher
            }
        }
    }
    /** not empty and not "passive" */
    protected var bitsInvokable: Long = 0
    protected var bitsPassive: Long = 0
    protected val cyphers: Array<AbstractCypher> = Array(capacity) { EmptyCypher }
    /** last Invokable index + 1 */
    val invokableSize : Int get() = 64 - bitsInvokable.countLeadingZeroBits()

    companion object {
        /** O(n) */
        fun of(list: List<AbstractCypher?>) = ArrayOfCyphers(list)

        const val MAX_LENGTH = 64 // max length capped at a Long-bits count, guess this is quite enough
    }
    init {
        // let's make sure capa > 0
        require(capacity in 1..MAX_LENGTH)
    }
    /** O(n) */
    constructor(list: List<AbstractCypher?>) : this(list.size) {
        list.withIndex().forEach { (i, cypher) ->
            if (cypher == null) return@forEach
            cyphers[i] = cypher

            if (cypher.isInvokable()) {
                bitsInvokable = bitsInvokable or (1L shl i)
            } else if (cypher.isPassive()) {
                bitsPassive = bitsPassive or (1L shl i)
            }
        }
    }

    // this allows index-get: myInventory[0]
    operator fun get(index: Int): AbstractCypher = cyphers[index]
    fun getInvokableOrNull(index: Int): AbstractCypher? {
        if (index >= capacity) return null
        val cy = this[index]
        return if (cy.isPassive() || cy.isEmpty()) null else cy
    }
    // this allows for-loop
    override fun iterator(): Iterator<AbstractCypher> = cyphers.iterator()
    // this allows index-set: myInventory[0] = FireballCypher()
    operator fun set(index: Int, cypher0: AbstractCypher?) {
        val cypher = cypher0 ?: EmptyCypher
        cyphers[index] = cypher

        if (cypher.isInvokable()) bitsInvokable = bitsInvokable or (1L shl index)
        if (cypher.isPassive()) bitsPassive = bitsPassive or (1L shl index)
        if (cypher.isEmpty()) {
            bitsInvokable = bitsInvokable and (1L shl index).inv()
            bitsPassive = bitsPassive and (1L shl index).inv()
        }
    }
    fun remove(index: Int) = set(index, null)
    /** O(n) */
    fun toList(): List<AbstractCypher> = cyphers.toList()
    /** Empty -> null */
    fun toNullableList(): List<AbstractCypher?> = cyphers.map { if (it.isEmpty()) null else it }
    fun copy(): ArrayOfCyphers = ArrayOfCyphers(cyphers.toList())
    fun isEmpty() = (bitsInvokable or bitsPassive) == 0L
    fun isNotEmpty() = !isEmpty()

    /** find the first Empty then replace that with given cypher
     * @return the replaced index, -1 if no empty */
    fun add(cypher: AbstractCypher) : Int {
        // O(1) due to _bit-s
        val first = (bitsInvokable or bitsPassive).inv().countTrailingZeroBits()
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



    /** gets a bit representation of cyphers, non-invokable set to 0 */
    fun bits(): Long {
        // O(1) due to _bit-s
        return bitsInvokable
    }

//    fun firstInvokable(): AbstractCypher? {
//        val i = _bitInvokable.countTrailingZeroBits()
//        if (i < capacity) {
//            return _cyphers[i]
//        }
//        return null
//    }
//    fun lastInvokable(): AbstractCypher? {
//        val i = 63 - _bitInvokable.countLeadingZeroBits()
//        if (i in 0..<capacity) {
//            return _cyphers[i]
//        }
//        return null
//    }
    fun nextInvokableIndex(startFrom: Int = 0): Int {
        val l = (bitsInvokable shr startFrom).countTrailingZeroBits()
        val t = startFrom + l
        return if (t < capacity) t else -1
    }
    fun nextInvokable(startFrom: Int = 0): AbstractCypher? {
        val i = nextInvokableIndex(startFrom)
        if (i < 0) return null
        return cyphers[i]
    }


    override fun toString() = cyphers.toList().toString()
}