package com.github.nahnullscience.cypher_nexus.content.cypher.wand_module

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.cypher.wand_module.modules.PrimaryInvokingModule
import com.github.nahnullscience.cypher_nexus.content.cypher.wand_module.modules.SecondaryEmptyModule
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.PRIMARY_MODULE
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.SECONDARY_MODULE
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.WandModuleCypher
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.ModuleSlot

object PrimaryInvokingCypher : WandModuleCypher() {
    override val resource = CypherNexus.modResource("primary_invoking")
    override val moduleSlots: Array<ModuleSlot<*>> = arrayOf(
        ModuleSlot(PRIMARY_MODULE, ::PrimaryInvokingModule),
        ModuleSlot(SECONDARY_MODULE, ::SecondaryEmptyModule)
    )
}