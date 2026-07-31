package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.types

import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.SPECIAL_MODULE
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractInputModule

abstract class AbstractSpecialInputModule() : AbstractInputModule() {
    final override val moduleType = SPECIAL_MODULE
}