package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import java.util.function.Supplier
import kotlin.collections.iterator

class HookContainer (
    val type: HookModule.HookType
) {
    // TODO consider sync hook-map to client
    private val _map = HashMap<HookModule<*>, LinkedHashMap<AbstractCypher, Int>>()

    fun add(cypher: AbstractCypher) {
        for (module in cypher.implementHooks) {
            add(module, cypher)
        }
    }
    fun add(module: HookModule<*>, cypher: AbstractCypher) {
        if (module.type != type) return

        if (module.hook.isInstance(cypher)) {
            // LinkedHashMap maintains a doubly-linked list of its entries to preserve insertion order
            val cypherMap = _map.getOrPut(module) { LinkedHashMap() }
            cypherMap[cypher] = cypherMap.getOrDefault(cypher, 0) + 1

        } else {
            // a cypher registered a HookModule it doesn't actually implement.
            CypherNexus.LOGGER.error("Cypher $cypher claimed to have module $module but doesn't implement it!")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(module: HookModule<T>): Map<T, Int> {
        val innerMap = _map[module] ?: return emptyMap()

        // because we strictly checked `isInstance` inside the add() method,
        // we mathematically guarantee that every AbstractCypher inside this specific
        // innerMap implements the interface 'T'. Therefore, we can cast the whole map safely
        return innerMap as Map<T, Int>
    }
    fun <T : Any> get(module: Supplier<out HookModule<T>>) = get(module.get())


    /** apply hooks through side effect */
    inline fun <T : Any> playHooks(module: HookModule<T>, action: (T, Int) -> Unit) {
        if (module.type != type) {
            CypherNexus.LOGGER.error("Try to play hooks $module of the wrong type!")
            return
        }
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
        if (module.type != type) {
            CypherNexus.LOGGER.error("Try to cumulate hooks $module of the wrong type!")
            return initial
        }

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