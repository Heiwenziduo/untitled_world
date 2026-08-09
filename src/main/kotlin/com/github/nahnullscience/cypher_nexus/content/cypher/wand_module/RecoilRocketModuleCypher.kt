package com.github.nahnullscience.cypher_nexus.content.cypher.wand_module

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.cypher.wand_module.modules.RecoilRocketModule
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.WandModuleCypher
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.ModuleSlot

object RecoilRocketModuleCypher : WandModuleCypher() {
    override val resource = CypherNexus.modResource("recoil_rocket")
    override val moduleSlots: Array<ModuleSlot<*>> = arrayOf(
        ModuleSlot(WandModuleTypes.RECOIL_MODULE, ::RecoilRocketModule),
    )
}