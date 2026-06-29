package com.github.nahnullscience.cypher_nexus.content.cypher.utility

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType

// TODO
abstract class AbstractDistanceInvoke(
    private val _manaDrain: Float
) : AbstractProjectileCypher<DedicatedCypherProjectile>() {
    override val category = CypherCategories.UTILITY

    override fun addToStateChunk(chunk: ShotStateChunk): ShotStateChunk {
        val subState = ShotStateChunk()
        chunk.addProjectile(ProjectileNode(this, subState, TriggerType.DEATH))
        return subState
    }
}