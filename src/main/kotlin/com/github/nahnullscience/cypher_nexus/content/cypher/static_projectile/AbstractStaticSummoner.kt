package com.github.nahnullscience.cypher_nexus.content.cypher.static_projectile

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.StaticProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothBeforeDiscardHook

abstract class AbstractStaticSummoner(
    override val manaDrain: Float
) : StaticProjectileCypher(manaDrain), BothBeforeDiscardHook {
    init {
        addFlag(CypherFlags.PIERCE_ENTITY)
        addFlag(CypherFlags.CONSTANT_EXISTING)
        addAttribute(CypherAttributes.EXISTING, 1.0)
    }

}