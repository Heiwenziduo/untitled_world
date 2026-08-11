package com.github.nahnullscience.cypher_nexus.content.cypher.utility

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModEntities.CYPHER_DISTANCE_DELIVERER
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType


abstract class AbstractDistanceDeliver(
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
) : AbstractProjectileCypher<AbstractDedicatedCypherProjectile>(defaultAttribute) {
    final override val category = CypherCategories.UTILITY
    final override val projectileType = CYPHER_DISTANCE_DELIVERER

    override val innateTrigger: TriggerType = TriggerType.DEATH

    class LongDistanceProjection(defaultAttribute: Builder.() -> Builder) : AbstractDistanceDeliver(defaultAttribute) {
        override val resource = CypherNexus.modResource("long_distance_projection")
    }
}