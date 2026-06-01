package com.github.nahnullscience.cypher_nexus.content.cypher.static_projectile

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataAttach
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.StaticProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothBeforeDiscardHook

abstract class AbstractStaticSummoner() : StaticProjectileCypher(), BothBeforeDiscardHook {

    override fun defaultAttributes(): CypherDataAttach.Builder {
        return super.defaultAttributes()
            .flags(CypherFlags.PIERCE_ENTITY)
            .flags(CypherFlags.CONSTANT_EXISTING)
            .projectileAttr(CypherAttributes.EXISTING, 2.0)
    }
}