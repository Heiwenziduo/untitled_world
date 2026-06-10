package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories

abstract class WandModuleCypher : AbstractNonProjectileCypher() {
    final override val category = CypherCategories.WAND_MODULE
    final override fun isInvokable() = false
    final override fun triggerInterplay() = false
}