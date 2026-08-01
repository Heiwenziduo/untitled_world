package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.ModuleSlot

abstract class WandModuleCypher : AbstractNonProjectileCypher() {
    final override val category = CypherCategories.WAND_MODULE
    final override val isInvokable = false
    final override fun triggerInterplay() = false

    /**
     *
     * */
    abstract val moduleSlots: Array<ModuleSlot<*>>
}