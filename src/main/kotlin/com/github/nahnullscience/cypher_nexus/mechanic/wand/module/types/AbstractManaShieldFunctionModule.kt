package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.types

import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.MANA_SHIELD_MODULE
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractFunctionalModule

abstract class AbstractManaShieldFunctionModule : AbstractFunctionalModule() {
    final override val moduleType = MANA_SHIELD_MODULE
}