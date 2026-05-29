package com.github.nahnullscience.cypher_nexus.content.cypher.utility

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType

// TODO
abstract class AbstractDistanceInvoke(
    override val manaDrain: Float
) : AbstractProjectileCypher() {
    override val draw: Int = 1
    override val category = CypherCategories.UTILITY

    override fun modifyStateChunk(helper: InvokingHelper, chunk: ProjectileStateChunk) = Unit

    override fun addToStateChunk(helper: InvokingHelper, chunk: ProjectileStateChunk): ProjectileStateChunk {
        val subState = ProjectileStateChunk()
        chunk.addProjectile(ProjectileNode(this, subState, TriggerType.DEATH))
        return subState
    }
}