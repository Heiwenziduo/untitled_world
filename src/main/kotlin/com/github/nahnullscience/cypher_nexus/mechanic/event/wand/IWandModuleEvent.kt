package com.github.nahnullscience.cypher_nexus.mechanic.event.wand

import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.WandModuleType

interface IWandModuleEvent {
    val type: WandModuleType<*>
}