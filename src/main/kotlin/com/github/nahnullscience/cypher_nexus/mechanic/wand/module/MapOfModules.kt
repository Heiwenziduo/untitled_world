package com.github.nahnullscience.cypher_nexus.mechanic.wand.module

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractWandModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.ITypeUniqueModule
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap
import java.util.function.Supplier

/**
 * stand as a module manager of a specific [ItemWandInstance]
 * */
class MapOfModules(
    val instance: ItemWandInstance
) {
    companion object {
        private typealias ModuleR2RMap = Reference2ReferenceOpenHashMap<WandModuleType<*>, AbstractWandModule>
    }

    @PublishedApi
    internal val uniqueModules = ModuleR2RMap(8).also { it.defaultReturnValue(null) }

    @PublishedApi
    internal val stackableModules: ModuleR2RMap get() {
        return _stackableModulesBacking ?: ModuleR2RMap(8)
            .also { it.defaultReturnValue(null) }
            .also { _stackableModulesBacking = it }
    }
    private var _stackableModulesBacking: ModuleR2RMap? = null


//    @PublishedApi
//    internal val modules = IdentityHashMap<WandModuleType<*>, IWandModule>(32)
    @PublishedApi
    internal var init = false

    operator fun <T> get(holder: Supplier<out WandModuleType<T>>): T? where T : AbstractWandModule, T : ITypeUniqueModule = get(holder.get())
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: WandModuleType<T>): T? where T : AbstractWandModule, T : ITypeUniqueModule {
        return uniqueModules[key] as T?
    }

    fun <T> addUniqueModule(holder: Supplier<out WandModuleType<T>>, module: T): T? where T : AbstractWandModule, T : ITypeUniqueModule = addUniqueModule(holder.get(), module)
    @Suppress("UNCHECKED_CAST")
    fun <T> addUniqueModule(key: WandModuleType<T>, module: T): T? where T : AbstractWandModule, T : ITypeUniqueModule {
        init = false
        return uniqueModules.put(key, module) as T?
    }

    /**
     * if the given module type doesn't present in the map,
     * execute the supplier and associate the result with the type, then return the result module.
     * otherwise return the present module.
     * */
    @Deprecated("use registerSlot instead for better type check")
    inline fun <T> getOrPut(
        holder: Supplier<out WandModuleType<T>>,
        moduleSupplier: () -> T
    ): T? where T : AbstractWandModule, T : ITypeUniqueModule = getOrPut(holder.get(), moduleSupplier)
    /**
     * if the given module type doesn't present in the map,
     * execute the supplier and associate the result with the type, then return the result module.
     * otherwise return the present module.
     * */
    @Deprecated("use registerSlot instead for better type check")
    inline fun <T> getOrPut(
        key: WandModuleType<T>,
        moduleSupplier: () -> T
    ): T? where T : AbstractWandModule, T : ITypeUniqueModule {
        return if (uniqueModules.containsKey(key)) { this[key] } else {
            val module = moduleSupplier()
            addUniqueModule(key, module)
            module
        }
    }

    /**
     * lazily install a module: [ModuleSlot.factory] only runs if [ModuleSlot.type]'s slot is free.
     * returns whichever module now occupies the slot — the freshly-built one, or the one that beat it there.
     * */
    fun <T> registerSlot(slot: ModuleSlot<T>): T where T : AbstractWandModule, T : ITypeUniqueModule {
        this[slot.type]?.let { return it }
        init = false
        val module = slot.factory(instance)
        addUniqueModule(slot.type, module)
        return module
    }

    fun finalizeInit() {
        init = true
//        CypherNexus.debugWand { "$instance computed modules, current module: $this" }
    }

    fun clear() {
        init = false
        uniqueModules.clear()
        _stackableModulesBacking?.clear()
    }

    override fun toString() = "$uniqueModules" + "${_stackableModulesBacking ?: ""}"
}