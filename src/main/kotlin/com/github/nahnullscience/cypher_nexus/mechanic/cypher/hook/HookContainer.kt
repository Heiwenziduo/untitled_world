package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import java.util.Optional
import java.util.function.Supplier
import kotlin.collections.iterator
import kotlin.jvm.optionals.getOrNull

class HookContainer (
    /**
     * the "prototype", use to isolate projectile specific hooks (child) from StateBlock hooks (parent),
     * while iteration, search this(child) first, then parents
     *  */
    val parent: Optional<HookContainer> = Optional.empty()
) {
    constructor(map: HashMap<HookModule<*>, LinkedHashMap<AbstractCypher, Int>>, parent: Optional<HookContainer> = Optional.empty()) : this(parent) {
        _map = map
    }

    /* @doc
     * LinkedHashMap maintains a doubly-linked list of its entries to preserve insertion order.
     * The least recently inserted entry (the eldest) is first, and the youngest entry is last.
     * The encounter order is not affected if a key is re-inserted into the map with the put method.
     * */
    private var _map = HashMap<HookModule<*>, LinkedHashMap<AbstractCypher, Int>>()

    fun add(cypher: AbstractCypher) {
        for (module in cypher.implementedHooks) {
            add(module, cypher)
        }
    }
    fun add(module: HookModule<*>, cypher: AbstractCypher) {
        if (module.hook.isInstance(cypher)) {
            val cypherMap = _map.getOrPut(module) { LinkedHashMap() }
            cypherMap[cypher] = cypherMap.getOrDefault(cypher, 0) + 1
        } else {
            // a cypher registered a HookModule it doesn't actually implement.
            CypherNexus.LOGGER.error("Cypher $cypher claimed to have module $module but doesn't implement it!")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(module: HookModule<T>): Map<T, Int> {
        val rawChild = _map[module]
        val childMap: Map<T, Int> = if (rawChild != null) rawChild as Map<T, Int> else emptyMap()
        val parentMap = parent.getOrNull()?.get(module)
        // because we strictly checked `isInstance` inside the add() method,
        // we mathematically guarantee that every AbstractCypher inside this specific
        // innerMap implements the interface 'T'. Therefore, we can cast the whole map safely

        if (parentMap.isNullOrEmpty()) return childMap
        if (childMap.isEmpty()) return parentMap

        // pre-allocate the capacity to avoid the internal array resizing overhead
        val merged = HashMap<T, Int>(parentMap.size + childMap.size)
        merged.putAll(parentMap)

        for ((hook, childCount) in childMap) {
            val parentCount = merged[hook] ?: 0
            merged[hook] = parentCount + childCount
        }
        return merged as Map<T, Int>
    }
    fun <T : Any> get(module: Supplier<out HookModule<T>>) = get(module.get())


    /** apply hooks through side effect */
    inline fun <T : Any> playHooks(module: HookModule<T>, action: (T, Int) -> Unit) {
        for ((hook, level) in get(module)) {
            action(hook, level)
        }
    }
    /** apply hooks through side effect */
    inline fun <T : Any> playHooks(module: Supplier<out HookModule<T>>, action: (T, Int) -> Unit) {
        playHooks(module.get(), action)
    }

    /** apply hooks by accumulate their values */
    inline fun <T : Any, R> cumulateHooks(module: HookModule<T>, initial: R, action: (T, Int, R) -> R): R {
        var accumulator = initial
        for ((hook, level) in get(module)) {
            // Pass the current accumulator into the action, and update the accumulator
            // with the return value for the next iteration.
            accumulator = action(hook, level, accumulator)
        }
        return accumulator
    }
    /** apply hooks by accumulate their values */
    inline fun <T : Any, R> cumulateHooks(module: Supplier<out HookModule<T>>, initial: R, action: (T, Int, R) -> R): R {
        return cumulateHooks(module.get(), initial, action)
    }
}