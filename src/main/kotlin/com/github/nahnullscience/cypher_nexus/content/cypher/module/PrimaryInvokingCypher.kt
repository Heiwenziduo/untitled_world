package com.github.nahnullscience.cypher_nexus.content.cypher.module

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.WandModuleCypher
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.IWandModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.ModuleCategory
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.ModulePrimaryInvoking
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.ModuleSecondaryEmpty
import java.util.*

object PrimaryInvokingCypher : WandModuleCypher() {
    override val resource = CypherNexus.modResource("primary_invoking")
    override fun apply(modules: EnumMap<ModuleCategory, IWandModule>) {
        modules.getOrPut(ModuleCategory.PRIMARY) { ModulePrimaryInvoking }
        modules.getOrPut(ModuleCategory.SECONDARY) { ModuleSecondaryEmpty }
    }
}