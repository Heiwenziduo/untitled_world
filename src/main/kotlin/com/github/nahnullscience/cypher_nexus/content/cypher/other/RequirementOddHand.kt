package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk

object RequirementOddHand : AbstractRequirement.RequirementIf() {
    override fun requirement(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: InvokingHelper.HelperDataBundle,
        state: InvokingHelper.InvokingStateBundle,
    ): Boolean {
        return helper.data.hand.countOneBits() and 1 > 0 // odd number cards in hand, go
    }

    override val resource = CypherNexus.modResource("requirement_odd_hand")
}