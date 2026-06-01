package com.github.nahnullscience.cypher_nexus.content.cypher.utility

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType

// TODO
abstract class AbstractDistanceInvoke(
    private val _manaDrain: Float
) : AbstractProjectileCypher() {
    override val category = CypherCategories.UTILITY

    override fun addToStateChunk(helper: InvokingHelper, chunk: ProjectileStateChunk): ProjectileStateChunk {
        val subState = ProjectileStateChunk()
        chunk.addProjectile(ProjectileNode(this, subState, TriggerType.DEATH))
        return subState
    }
}