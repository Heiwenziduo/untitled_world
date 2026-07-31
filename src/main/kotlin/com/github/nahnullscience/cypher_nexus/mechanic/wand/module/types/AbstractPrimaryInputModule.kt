package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.types

import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.PRIMARY_MODULE
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractInputModule

abstract class AbstractPrimaryInputModule() : AbstractInputModule() {
    final override val moduleType = PRIMARY_MODULE
}