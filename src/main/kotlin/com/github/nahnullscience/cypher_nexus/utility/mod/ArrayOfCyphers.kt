package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories.WAND_MODULE_RESOURCE
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.EmptyCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.WandModuleCypher


/**
 * a helper Array that provides a series of utils to manipulate cyphers.
 * fixed length, cypher changeable, EmptyCypher autofill, quick lookup, and bits operation-friendly
 * */
open class ArrayOfCyphers(val capacity: Int) : Iterable<AbstractCypher> {
    companion object {
        private fun AbstractCypher.isModule(): Boolean = isNotEmpty() && category.`is`(WAND_MODULE_RESOURCE)

        const val MAX_LENGTH = Long.SIZE_BITS // max length capped at a Long-bits count (64), guess this is quite enough

        /**
         * access an Array indices through 1-bits of a Long
         * */
        inline fun <T> Array<T>.bitForEach(bitsAccess: Long, action: (index: Int, element: T) -> Unit) {
            if (size > 64) CypherNexus.warn { "${this.contentToString()} uses a bit access but its size exceeds 64!" }

            var mask =
                if (size < 64) bitsAccess and (-1L shl size).inv()
                // for Kotlin use "size % 64" to wrap the distance,
                // -1L shl 64 <=> -1L shl 0 which will result in 111...111 the 64 1s
                // thus, x and -1L.inv() will wipe out all bits
                else bitsAccess


            while (mask != 0L) {
                val index = mask.countTrailingZeroBits()
                action(index, this[index])
                mask = mask and (mask - 1)
            }
        }

        /**
         * access an Array indices through 1-bits of a Long, last element first
         * */
        inline fun <T> Array<T>.bitForEachReverse(bitsAccess: Long, action: (index: Int, element: T) -> Unit) {
            if (size > 64) CypherNexus.warn { "${this.contentToString()} uses a bit access but its size exceeds 64!" }

            var mask =
                if (size < 64) bitsAccess and (-1L shl size).inv()
                else bitsAccess

            while (mask != 0L) {
                val index = 63 - mask.countLeadingZeroBits()
                action(index, this[index])
                mask = mask xor (1L shl index)
            }
        }
    }

    /** O(n) */
    constructor(list: List<AbstractCypher?>) : this(list.size) {
        list.withIndex().forEach { (i, cypher) ->
            if (cypher == null) return@forEach
            cyphers[i] = cypher

            if (cypher.isInvokable) {
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

    @PublishedApi
    internal val cyphers: Array<AbstractCypher> = Array(capacity) { EmptyCypher }
    /** not empty and not "passive" */
    var bitsInvokable: Long = 0
    protected set
    var bitsModule: Long = 0
    protected set
    /** the last Invokable index + 1 */
    val invokableSize : Int get() = 64 - bitsInvokable.countLeadingZeroBits()


    // this allows index-get: myInventory[0]
    operator fun get(index: Int): AbstractCypher = cyphers[index]
    /**
     * Returns an element at the given [index] or `null` if the [index] is out of bounds of this array.
     * */
    fun getOrNull(index: Int): AbstractCypher? = cyphers.getOrNull(index)
    fun getInvokableOrNull(index: Int): AbstractCypher? {
        if (index >= capacity) return null
        val cy = this[index]
        return if (cy.isInvokable) cy else null
    }
    // this allows for-loop
    override fun iterator(): Iterator<AbstractCypher> = cyphers.iterator()

    /** O(n) */
    fun toList(): List<AbstractCypher> = cyphers.toList()
    /** Empty -> null */
    fun toNullableList(): List<AbstractCypher?> = cyphers.map { if (it.isEmpty()) null else it }
    /**
     * @return a mutable `copy` of this [ArrayOfCyphers]
     * */
    fun toMutable(): MutableAoC = MutableAoC(this)
    open fun copy(): ArrayOfCyphers = toMutable()
    fun isEmpty() = (bitsInvokable or bitsModule) == 0L
    fun isNotEmpty() = !isEmpty()
    fun isInvokable() = bitsInvokable != 0L

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
                if (index < capacity) {
                    val cy = this@ArrayOfCyphers[index]
                    if (cy.isModule()) yield(cy as WandModuleCypher)
                }

                mask = mask and (mask - 1)
            }
        }
    }


    /**
     * pop every Module Cyphers in reverse order
     * */
    fun modulesSequenceReverse(): Sequence<WandModuleCypher> {
        return sequence {
            var mask = bitsModule
            while (mask != 0L) {
                val index = 63 - mask.countLeadingZeroBits()
                val cy = this@ArrayOfCyphers[index]
                if (cy.isModule()) yield(cy as WandModuleCypher)

                mask = mask xor (1L shl index)
            }
        }
    }


    /**
     * pop every invokable cyphers in order
     * @param merge a mark uses And operator to filter the bits
     *  */
    fun invokableSequence(merge: Long): Sequence<AbstractCypher> {
        return sequence {
            var mask = bitsInvokable and merge
            while (mask != 0L) {
                val index = mask.countTrailingZeroBits()
                if (index < capacity) {
                    val cy = this@ArrayOfCyphers[index]
                    if (cy.isInvokable) yield(cy)
                }

                mask = mask and (mask - 1)
            }
        }
    }


    /**
     * pop every invokable cyphers in reverse order
     * @param merge a mark uses And operator to filter the bits
     * */
    fun invokableSequenceReverse(merge: Long): Sequence<AbstractCypher> {
        return sequence {

        }
    }


    /**
     * do side effects on every invokable cypher in order.
     * compare to sequence, this can naturally access the index of elements
     * @param merge a mark uses And operator to filter the bits, leave it empty and will go through all
     *  */
    inline fun invokableForEach(merge: Long = -1L, action: (index: Int, cypher: AbstractCypher) -> Unit) {
        this.cyphers.bitForEach(bitsInvokable and merge, action)
        return
    }

    /**
     * do side effects on every invokable cypher in reverse order.
     * compare to sequence, this can naturally access the index of elements
     * @param merge a mark uses And operator to filter the bits, leave it empty and will go through all
     *  */
    inline fun invokableForEachReverse(merge: Long = -1L, action: (index: Int, cypher: AbstractCypher) -> Unit) {
        this.cyphers.bitForEachReverse(bitsInvokable and merge, action)
        return
    }


    fun firstInvokable(): AbstractCypher? {
        val i = bitsInvokable.countTrailingZeroBits()
        if (i < capacity) {
            return cyphers[i]
        }
        return null
    }
    fun lastInvokable(): AbstractCypher? {
        val i = 63 - bitsInvokable.countLeadingZeroBits()
        if (i in 0 until capacity) {
            return cyphers[i]
        }
        return null
    }
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
    /**
     * @param startFrom the index to start count from, assume it's within 0..63
     * @return the index of the first [EmptyCypher], -1 if there's no empty slot.
     * */
    fun firstEmptyIndex(startFrom: Int = 0): Int {
        val empties = (bitsInvokable or bitsModule).inv().shr(startFrom)
        val r = startFrom + empties.countTrailingZeroBits()
        return if (r < capacity) r else -1
    }


    override fun toString() = cyphers.toList().toString()


    /**
     *
     * */
    class MutableAoC(list: List<AbstractCypher?>) : ArrayOfCyphers(list) {
        constructor(aoc: ArrayOfCyphers) : this(aoc.toList())


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

        // this allows index-set: myInventory[0] = FireballCypher()
        operator fun set(index: Int, cypher0: AbstractCypher?) {
            val cypher = cypher0 ?: EmptyCypher
            cyphers[index] = cypher

            if (cypher.isInvokable) bitsInvokable = bitsInvokable or (1L shl index)
            if (cypher.isModule()) bitsModule = bitsModule or (1L shl index)
            if (cypher.isEmpty()) {
                bitsInvokable = bitsInvokable and (1L shl index).inv()
                bitsModule = bitsModule and (1L shl index).inv()
            }
        }
        fun remove(index: Int) = set(index, null)

        fun clearAll() {
            bitsInvokable = 0
            bitsModule = 0
            for (i in cyphers.indices) {
                cyphers[i] = EmptyCypher
            }
        }

        /**
         * @return a copy of this [MutableAoC], changes made to the copy won't affect the original
         * */
        override fun copy(): MutableAoC = MutableAoC(toList())
    }
}