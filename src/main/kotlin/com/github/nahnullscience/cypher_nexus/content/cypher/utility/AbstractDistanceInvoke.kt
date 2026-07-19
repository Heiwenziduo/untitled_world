package com.github.nahnullscience.cypher_nexus.content.cypher.utility

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType

// TODO
abstract class AbstractDistanceInvoke(
    private val _manaDrain: Float
) : AbstractProjectileCypher<AbstractDedicatedCypherProjectile>() {
    override val category = CypherCategories.UTILITY

    override val builtinTrigger: TriggerType = TriggerType.DEATH
}