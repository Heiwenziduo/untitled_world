package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.WandModuleType
import java.util.function.Supplier

sealed class AbstractWandModule {

    /**
     * registered wand module type
     * */
    abstract val moduleType: Supplier<out WandModuleType<*>>

    /**
     * instance reference
     * */
    abstract val instance: ItemWandInstance


    fun isTypeOf(type: Supplier<out WandModuleType<*>>) = type == moduleType
}

