package com.github.nahnullscience.cypher_nexus.content.cypher.utility

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk

object RefresherRingCypher : AbstractNonProjectileCypher() {
    override val resource = CypherNexus.modResource("refresher_ring")
    override val category = CypherCategories.UTILITY
    override val isRecursive = true

    override fun invokeInHand(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: InvokingHelper.HelperDataBundle,
        state: InvokingHelper.HelperStateBundle,
        options: CypherInvokingOptions
    ) {
        CypherNexus.LOGGER.debug("[{}] is invoked", this)
        if (state.alreadyRefreshed) {
            // terminate invoking process if meet again
            // this only prevent drawing new cards, current invoking cypher will continue its function
            helper.reload()
            return
        }
        state.alreadyRefreshed = true
        helper.init()
    }
}