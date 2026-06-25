package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import java.util.*
import java.util.function.Supplier

class MapOfModules(
    val instance: ItemWandInstance
) {
    @PublishedApi
    internal val modules = IdentityHashMap<WandModuleType<*>, IWandModule>(16)
    @PublishedApi
    internal var init = false

    @Suppress("UNCHECKED_CAST")
    operator fun <T : IWandModule> get(key: WandModuleType<T>): T? {
        return modules[key] as T?
    }
    operator fun <T : IWandModule> get(holder: Supplier<out WandModuleType<T>>): T? {
        return get(holder.get())
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : IWandModule> putAndOverwrite(key: WandModuleType<T>, module: T): T? {
        init = false
        return modules.put(key, module) as T?
    }
    fun <T : IWandModule> putAndOverwrite(holder: Supplier<out WandModuleType<T>>, module: T): T? {
        return putAndOverwrite(holder.get(), module)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <T : IWandModule> getOrPut(key: WandModuleType<T>, moduleSupplier: () -> T): T? {
        return if (modules.containsKey(key)) {
            modules[key] as T?
        } else {
            init = false
            val module = moduleSupplier()
            putAndOverwrite(key, module)
            module
        }
    }
    inline fun <T : IWandModule> getOrPut(holder: Supplier<out WandModuleType<T>>, moduleSupplier: () -> T): T? {
        return getOrPut(holder.get(), moduleSupplier)
    }

    fun finalizeInit() {
        init = true
        CypherNexus.debugWand { "$instance computed modules, current module: $this" }
    }

    fun clear() {
        init = false
        modules.clear()
    }

    override fun toString() = modules.toString()

}