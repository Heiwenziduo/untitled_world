package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.IRecursiveCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingStateBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk

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
        chunk: ProjectileStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle,
        relativeIndex: Int,
        isCopy: Boolean
    ) {
        CypherNexus.debugCypher { "[$this $relativeIndex] is invoked and modifies the state" }

        modifyStateChunk(helper, data, chunk)

        duplicate(helper, chunk, data, state, relativeIndex)

        handleDraws(helper, chunk, data, state)
    }

    // re-invoke every cypher in Hand
    private fun duplicate(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle,
        relativeIndex: Int,
    ) {
        val remember = data.hand
        helper.aoc.invokableForEach(remember) { index, cypher ->
            if (cypher is CypherDuplicationCypher) return@invokableForEach
            copyCypher(cypher, helper, chunk, data, state, index)
        }
    }
}