package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import java.util.function.Supplier

class HookContainer (
//    /**
//     * the "prototype", use to isolate projectile specific hooks (child) from StateBlock hooks (parent),
//     * while iteration, search this(child) first, then parents
//     *  */
//    val parent: Optional<HookContainer> = Optional.empty()
) {
    companion object {
        typealias HookAction <T> = (index: Int, hook: T, count: Int) -> Unit
        typealias HookCumulates <T, R> = (index: Int, hook: T, count: Int, cumulate: R) -> R
    }

    @PublishedApi
    internal val hooks = Reference2ReferenceOpenHashMap<HookModule<*>, OrderedCypherCounts>()

    override fun toString() = hooks.toString()

    fun add(cypher: AbstractCypher, count: Int = 1) {
        for (module in cypher.implementedHooks) {
            add(module, cypher, count)
        }
    }
    private fun add(module: HookModule<*>, cypher: AbstractCypher, count: Int = 1) {
        hooks.compute(module) { module, occ ->
            occ?.add(cypher, count) ?: OrderedCypherCounts().add(cypher, count)
        }
    }

//    @Suppress("UNCHECKED_CAST")
//    fun <T : Any> get(module: HookModule<T>): Map<T, Int> {
//        Profiler.get().incrementCounter { "cypherHookAccess" }
//
//        val rawChild = _map[module]
//        val childMap: Map<T, Int> = if (rawChild != null) rawChild as Map<T, Int> else emptyMap()
//        val parentMap = parent.getOrNull()?.get(module)
//        // because we strictly checked `isInstance` inside the add() method,
//        // we mathematically guarantee that every AbstractCypher inside this specific
//        // innerMap implements the interface 'T'. Therefore, we can cast the whole map safely
//
//        if (parentMap.isNullOrEmpty()) return childMap
//        if (childMap.isEmpty()) return parentMap
//
//        // pre-allocate the capacity to avoid the internal array resizing overhead
//        val merged = HashMap<T, Int>(parentMap.size + childMap.size)
//        merged.putAll(parentMap)
//
//        for ((hook, childCount) in childMap) {
//            val parentCount = merged[hook] ?: 0
//            merged[hook] = parentCount + childCount
//        }
//
//        return merged as Map<T, Int>
//    }
//    fun <T : Any> get(module: Supplier<out HookModule<T>>) = get(module.get())

    operator fun <T : IHook> get(module: HookModule<T>): OrderedCypherCounts? = hooks[module]
    operator fun <T : IHook> get(module: Supplier<out HookModule<T>>) = this[module.get()]


    /** apply hooks through side effect */
    @Suppress("UNCHECKED_CAST")
    inline fun <T : IHook> playHooks(module: HookModule<T>, action: HookAction<T>) {
        this[module]?.forEachIndexed { index, cy, count ->
            action(index, cy as T, count)
        }
    }
    /** apply hooks through side effect */
    inline fun <T : IHook> playHooks(module: Supplier<out HookModule<T>>, action: HookAction<T>) = playHooks(module.get(), action)

    /** apply hooks by accumulate their values */
    @Suppress("UNCHECKED_CAST")
    inline fun <T : IHook, R> cumulateHooks(module: HookModule<T>, initial: R, action: HookCumulates<T, R>): R {
        var accumulator = initial
        this[module]?.forEachIndexed { index, cy, count ->
            accumulator = action(index, cy as T, count, accumulator)
        }
        return accumulator
    }
    /** apply hooks by accumulate their values */
    inline fun <T : IHook, R> cumulateHooks(module: Supplier<out HookModule<T>>, initial: R, action: HookCumulates<T, R>): R = cumulateHooks(module.get(), initial, action)

    /**
     *
     * */
     class OrderedCypherCounts {
        @PublishedApi
        internal val keys = ReferenceArrayList<AbstractCypher>()
        @PublishedApi
        internal val counts = IntArrayList()
        private val slotMapping = Reference2IntOpenHashMap<AbstractCypher>().apply { defaultReturnValue(-1) }

        /**
         * @return -1 if no such mapping, otherwise the count of given cypher
         * */
        operator fun get(cy: AbstractCypher): Int {
            val slot = slotMapping.getInt(cy)
            if (slot < 0) return slot
            return counts.getInt(slot)
        }

        fun add(cy: AbstractCypher, n: Int = 1): OrderedCypherCounts = apply {
            if (cy.isEmpty()) return@apply
            val slot = slotMapping.getInt(cy)
            if (slot < 0) {
                slotMapping.put(cy, keys.size)
                keys.add(cy)
                counts.add(n)
            }
            else counts.set(slot, counts.getInt(slot) + n)
        }

        inline fun forEach(action: (cy: AbstractCypher, count: Int) -> Unit) {
            for (i in keys.indices) action(keys[i], counts.getInt(i))
        }

        inline fun forEachIndexed(action: (index: Int, cy: AbstractCypher, count: Int) -> Unit) {
            for (i in keys.indices) action(i, keys[i], counts.getInt(i))
        }
    }
}