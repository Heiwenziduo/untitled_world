package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.IRecursiveCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingParameterBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk

object CypherDuplicationCypher : AbstractNonProjectileCypher(), IRecursiveCypher {
    override val resource = CypherNexus.modResource("cypher_duplication")
    override val category = CypherCategories.OTHER
    override val isRecursive = true

    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(300f)
            .draw(1)
            .delay(7)
            .recharge(7)
    }

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

        duplicate(helper, shotState, data, paras, relativeIndex)

        handleDraws(helper, shotState, data, paras)
    }

    // re-invoke every cypher in Hand
    private fun duplicate(
        helper: InvokingHelper,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingParameterBundle,
        relativeIndex: Int,
    ) {
        val remember = data.hand
        helper.aoc.invokableForEach(remember) { index, cypher ->
            if (cypher is CypherDuplicationCypher) return@invokableForEach
            copyCypher(cypher, helper, shotState, data, paras, index)
        }
    }
}