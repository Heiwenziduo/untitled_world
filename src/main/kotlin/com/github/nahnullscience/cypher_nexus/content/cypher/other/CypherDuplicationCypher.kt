package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.IRecursiveCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingSharedParameter
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk

class CypherDuplicationCypher(
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
) : AbstractNonProjectileCypher(defaultAttribute), IRecursiveCypher {
    override val resource = CypherNexus.modResource("cypher_duplication")
    override val category = CypherCategories.OTHER
    override val isRecursive = true

    override fun invoke(
        helper: InvokingHelper,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingSharedParameter,
        relativeIndex: Int,
        isCopy: Boolean
    ) {
        CypherNexus.debugCypher { "[$this $relativeIndex] is invoked and modifies the state" }

        modifyShotState(helper, shotState, data, paras, isCopy)

        duplicate(helper, shotState, data, paras, relativeIndex)

        handleDraws(helper, shotState, data, paras)
    }

    // re-invoke every cypher in Hand
    private fun duplicate(
        helper: InvokingHelper,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingSharedParameter,
        relativeIndex: Int,
    ) {
        val remember = data.hand
        helper.aoc.invokableForEach(remember) { index, cypher ->
            if (cypher is CypherDuplicationCypher) return@invokableForEach
            copyCypher(cypher, helper, shotState, data, paras, index)
        }
    }
}