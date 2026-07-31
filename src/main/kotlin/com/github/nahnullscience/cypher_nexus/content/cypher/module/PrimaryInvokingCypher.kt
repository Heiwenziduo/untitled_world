package com.github.nahnullscience.cypher_nexus.content.cypher.module

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.WandModuleCypher
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.MapOfModules

object PrimaryInvokingCypher : WandModuleCypher() {
    override val resource = CypherNexus.modResource("primary_invoking")

    override fun apply(instance: ItemWandInstance, modules: MapOfModules) {

    }
}