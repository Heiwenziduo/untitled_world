package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories.WAND_MODULE_RESOURCE
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.EmptyCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.WandModuleCypher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


private fun AbstractCypher.isModule(): Boolean = isNotEmpty() && category.`is`(WAND_MODULE_RESOURCE)

/**
 * fixed length, cypher changeable, EmptyCypher autofill
 * */
open class ArrayOfCyphers(val capacity: Int) : Iterable<AbstractCypher> {
    companion object {
        /** O(n) */
        fun of(list: List<AbstractCypher?>) = ArrayOfCyphers(list)

        const val MAX_LENGTH = 64 // max length capped at a Long-bits count, guess this is quite enough
    }

    /** O(n) */
    constructor(list: List<AbstractCypher?>) : this(list.size) {
        list.withIndex().forEach { (i, cypher) ->
            if (cypher == null) return@forEach
            cyphers[i] = cypher

            if (cypher.isInvokable()) {
                bitsInvokable = bitsInvokable or (1L shl i)
            } else if (cypher.isModule()) {
                bitsModule = bitsModule or (1L shl i)
            }
        }
    }

    init {
        // let's make sure capa > 0
        require(capacity in 1..MAX_LENGTH)
    }

    /** not empty and not "passive" */
    protected var bitsInvokable: Long = 0
    protected var bitsModule: Long = 0
    protected val cyphers: Array<AbstractCypher> = Array(capacity) { EmptyCypher }
    /** last Invokable index + 1 */
    val invokableSize : Int get() = 64 - bitsInvokable.countLeadingZeroBits()


    // this allows index-get: myInventory[0]
    operator fun get(index: Int): AbstractCypher = cyphers[index]
    fun getInvokableOrNull(index: Int): AbstractCypher? {
        if (index >= capacity) return null
        val cy = this[index]
        return if (cy.isModule() || cy.isEmpty()) null else cy
    }
    // this allows for-loop
    override fun iterator(): Iterator<AbstractCypher> = cyphers.iterator()
    // this allows index-set: myInventory[0] = FireballCypher()
    operator fun set(index: Int, cypher0: AbstractCypher?) {
        val cypher = cypher0 ?: EmptyCypher
        cyphers[index] = cypher

        if (cypher.isInvokable()) bitsInvokable = bitsInvokable or (1L shl index)
        if (cypher.isModule()) bitsModule = bitsModule or (1L shl index)
        if (cypher.isEmpty()) {
            bitsInvokable = bitsInvokable and (1L shl index).inv()
            bitsModule = bitsModule and (1L shl index).inv()
        }
    }
    fun remove(index: Int) = set(index, null)
    /** O(n) */
    fun toList(): List<AbstractCypher> = cyphers.toList()
    /** Empty -> null */
    fun toNullableList(): List<AbstractCypher?> = cyphers.map { if (it.isEmpty()) null else it }
    fun copy(): ArrayOfCyphers = ArrayOfCyphers(cyphers.toList())
    fun isEmpty() = (bitsInvokable or bitsModule) == 0L
    fun isNotEmpty() = !isEmpty()
    fun isInvokable() = bitsInvokable > 0

    /** find the first Empty then replace that with given cypher
     * @return the replaced index, -1 if no empty */
    fun add(cypher: AbstractCypher) : Int {
        // O(1) due to _bit-s
        val first = (bitsInvokable or bitsModule).inv().countTrailingZeroBits()
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

    /**
     * pop every Module Cyphers in order
     * */
    fun modulesSequence(): Sequence<WandModuleCypher> {
        return sequence {
            var mask = bitsModule

            while (mask != 0L) {
                val index = mask.countTrailingZeroBits()

                println(index)

                if (index < capacity) {
                    val cy = this@ArrayOfCyphers[index]
                    println(cy)
                    if (cy.isModule()) yield(cy as WandModuleCypher)
                }

                mask = mask and (mask - 1)
            }
        }
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

    class MutableAoC(val aoc: ArrayOfCyphers) : Iterable<AbstractCypher> by aoc {
        constructor(capacity: Int) : this(ArrayOfCyphers(capacity))
        // TODO consider move #set function here
        fun clearAll() {
            aoc.bitsInvokable = 0
            aoc.bitsModule = 0
            for (i in aoc.cyphers.indices) {
                aoc.cyphers[i] = EmptyCypher
            }
        }
    }
}