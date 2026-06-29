package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories

/**
 * default registered cypher, like blocks:air, any cypher missing a registry name will be replaced with this.
 * */
object EmptyCypher: AbstractCypher() {
    override val hide = true
    override val category = CypherCategories.OTHER
    override val resource = CypherNexus.modResource("empty_cypher")
    override val isInvokable = false
    override fun triggerInterplay() = false

}