package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.types

import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.INVOKE_MODULE
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractFunctionalModule

abstract class AbstractInvokeFunctionModule : AbstractFunctionalModule() {
    final override val moduleType = INVOKE_MODULE
}