package com.github.nahnullscience.cypher_nexus.content.cypher.utility

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.IRecursiveCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingParameterBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk

object RefresherRingCypher : AbstractNonProjectileCypher(), IRecursiveCypher {
    override val resource = CypherNexus.modResource("refresher_ring")
    override val category = CypherCategories.UTILITY
    override val isRecursive = true

    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(20f)
            .recharge(-8)
    }

    override fun triggerInterplay() = true
    override fun invoke(
        helper: InvokingHelper,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingParameterBundle,
        relativeIndex: Int,
        isCopy: Boolean
    ) {
        CypherNexus.debugCypher { "[$this $relativeIndex] is invoked and modifies the state" }
        modifyShotState(helper, data, shotState)

        if (paras.alreadyRefreshed) {
            // terminate invoking process if meet again
            // this only prevent drawing new cards, current invoking cypher will continue its function
            helper.reload()
            return
        }

        paras.alreadyRefreshed = true
        helper.init()
        data.recharge = 0
    }
}