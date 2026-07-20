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

object ProteusCypher : AbstractNonProjectileCypher(), IRecursiveCypher {
    override val resource = CypherNexus.modResource("proteus")
    override val category = CypherCategories.UTILITY
    override val isRecursive = false
    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(10f)
            .draw(1)
    }

    override fun triggerInterplay() = true

    /**
     * draw [draw] times, then copy drawn cyphers one time
     * */
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

        if (paras.drawEnabled)
            drawXForEach(helper, draw) { index, cypher ->
                cypher.invokeInHand(helper, shotState, data, paras)
                if (cypher is ProteusCypher) data.manaCurrent += 80f
                else copyCypher(cypher, helper, shotState, data, paras, index)
            }
    }
}