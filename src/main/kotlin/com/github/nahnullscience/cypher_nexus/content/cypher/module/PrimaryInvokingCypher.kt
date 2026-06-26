package com.github.nahnullscience.cypher_nexus.content.cypher.module

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.WandModuleCypher
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.content.cypher.module.modules.ModulePrimaryInvoking
import com.github.nahnullscience.cypher_nexus.content.cypher.module.modules.ModuleSecondaryEmpty
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.MapOfModules

object PrimaryInvokingCypher : WandModuleCypher() {
    override val resource = CypherNexus.modResource("primary_invoking")

    override fun apply(instance: ItemWandInstance, modules: MapOfModules) {
        modules.getOrPut(WandModuleTypes.PRIMARY) { ModulePrimaryInvoking(instance) }
        modules.getOrPut(WandModuleTypes.SECONDARY) { ModuleSecondaryEmpty(instance) }
    }
}