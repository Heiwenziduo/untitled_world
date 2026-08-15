package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingSharedParameter
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk

object RequirementOddHand : AbstractRequirement.RequirementIf() {
    override fun requirement(
        helper: InvokingHelper,
        shotState: ShotStateChunk,
        data: InvokingHelper.HelperDataBundle,
        paras: InvokingSharedParameter,
    ): Boolean {
        return helper.data.hand.countOneBits() and 1 > 0 // odd number cards in hand, go
    }

    override val resource = CypherNexus.modResource("requirement_odd_hand")
}