package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.types

import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.RECOIL_MODULE
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractFunctionalModule

abstract class AbstractRecoilFunctionModule : AbstractFunctionalModule() {
    final override val moduleType = RECOIL_MODULE
}